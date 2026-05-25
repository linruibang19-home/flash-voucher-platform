package org.javaup.order.kafka.consumer;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.javaup.api.notify.NotifyClient;
import org.javaup.consumer.AbstractConsumerHandler;
import org.javaup.core.RedisKeyManage;
import org.javaup.enums.BusinessType;
import org.javaup.enums.LogType;
import org.javaup.enums.SeckillVoucherOrderOperate;
import org.javaup.exception.HmdpFrameException;
import org.javaup.message.MessageExtend;
import org.javaup.message.SeckillVoucherMessage;
import org.javaup.order.redis.RedisVoucherData;
import org.javaup.order.service.IVoucherOrderService;
import org.javaup.order.service.IVoucherReconcileLogService;
import org.javaup.redis.RedisCache;
import org.javaup.redis.RedisKeyBuild;
import org.javaup.toolkit.SnowflakeIdGenerator;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @description: Kafka 消费者（order-service）：处理秒杀券下单消息，创建订单
 * @maintainer: lrb
 **/
@Slf4j
@Component
public class SeckillVoucherConsumer extends AbstractConsumerHandler<SeckillVoucherMessage> {

    /** Kafka 消息最大可接受延迟（毫秒）*/
    public static final long MESSAGE_DELAY_TIME = 10_000L;

    @Resource
    private IVoucherOrderService voucherOrderService;

    @Resource
    private IVoucherReconcileLogService voucherReconcileLogService;

    @Resource
    private RedisVoucherData redisVoucherData;

    @Resource
    private RedisCache redisCache;

    @Resource
    private NotifyClient notifyClient;

    @Resource
    private SnowflakeIdGenerator snowflakeIdGenerator;

    private static final int CPU_CORES = Runtime.getRuntime().availableProcessors();
    private static final ThreadPoolExecutor POST_CONSUME_EXECUTOR =
            new ThreadPoolExecutor(
                    Math.max(2, CPU_CORES),
                    Math.max(2, CPU_CORES),
                    0L,
                    TimeUnit.MILLISECONDS,
                    new LinkedBlockingQueue<>(1024 * Math.max(1, CPU_CORES)),
                    new NamedThreadFactory("order-consume-task", false),
                    new ThreadPoolExecutor.CallerRunsPolicy()
            );

    private static class NamedThreadFactory implements ThreadFactory {
        private final String namePrefix;
        private final boolean daemon;
        private final AtomicInteger index = new AtomicInteger(1);

        NamedThreadFactory(String namePrefix, boolean daemon) {
            this.namePrefix = namePrefix;
            this.daemon = daemon;
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, namePrefix + index.getAndIncrement());
            t.setDaemon(daemon);
            t.setUncaughtExceptionHandler((thread, ex) ->
                    log.error("未捕获异常，线程={}", thread.getName(), ex));
            return t;
        }
    }

    public SeckillVoucherConsumer() {
        super(SeckillVoucherMessage.class);
    }

    @KafkaListener(
            topics = {"${prefix.distinction.name:hmdp}-seckill_voucher_topic"},
            groupId = "${prefix.distinction.name:hmdp}-order-seckill-consumer"
    )
    public void onMessage(String value,
                          @Headers Map<String, Object> headers,
                          @Header(name = KafkaHeaders.RECEIVED_KEY, required = false) String key,
                          Acknowledgment acknowledgment) {
        consumeRaw(value, key, headers);
        if (acknowledgment != null) {
            acknowledgment.acknowledge();
        }
    }

    @Override
    protected Boolean beforeConsume(MessageExtend<SeckillVoucherMessage> message) {
        long delayTime = System.currentTimeMillis() - message.getProducerTime().getTime();
        if (delayTime > MESSAGE_DELAY_TIME) {
            log.info("消息延迟 {}ms 超过阈值，回滚 Redis 并丢弃订单={}", delayTime, message.getMessageBody().getOrderId());
            long traceId = snowflakeIdGenerator.nextId();
            SeckillVoucherMessage body = message.getMessageBody();
            redisVoucherData.rollbackRedisVoucherData(
                    SeckillVoucherOrderOperate.YES,
                    traceId,
                    body.getVoucherId(),
                    body.getUserId(),
                    body.getOrderId(),
                    body.getAfterQty(),
                    body.getChangeQty(),
                    body.getBeforeQty()
            );
            try {
                voucherReconcileLogService.saveReconcileLog(
                        LogType.RESTORE.getCode(),
                        BusinessType.TIMEOUT.getCode(),
                        "message delayed " + delayTime + "ms, rollback redis",
                        traceId,
                        message
                );
            } catch (Exception e) {
                log.warn("保存对账日志失败(延迟丢弃)", e);
            }
            return false;
        }
        return true;
    }

    @Override
    protected void doConsume(MessageExtend<SeckillVoucherMessage> message) {
        voucherOrderService.createVoucherOrder(message);
    }

    @Override
    protected void afterConsumeSuccess(MessageExtend<SeckillVoucherMessage> message) {
        super.afterConsumeSuccess(message);
        SeckillVoucherMessage body = message.getMessageBody();
        POST_CONSUME_EXECUTOR.execute(() -> {
            // 清理订阅 ZSet
            try {
                redisCache.delForSortedSet(
                        RedisKeyBuild.createRedisKey(RedisKeyManage.SECKILL_SUBSCRIBE_ZSET_TAG_KEY, body.getVoucherId()),
                        String.valueOf(body.getUserId())
                );
            } catch (Exception e) {
                log.warn("清理订阅ZSET失败 voucherId={} userId={}", body.getVoucherId(), body.getUserId(), e);
            }
            // 自动发券通知
            if (Boolean.TRUE.equals(body.getAutoIssue())) {
                try {
                    notifyClient.sendAutoIssueNotify(body.getVoucherId(), body.getUserId(), body.getOrderId());
                } catch (Exception e) {
                    log.warn("自动发券通知失败 voucherId={} orderId={}", body.getVoucherId(), body.getOrderId(), e);
                }
            }
            // 更新商店每日 Top 买家榜
            try {
                Long shopId = resolveShopId(body.getVoucherId());
                if (shopId != null) {
                    String day = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
                    RedisKeyBuild dailyKey = RedisKeyBuild.createRedisKey(
                            RedisKeyManage.SECKILL_SHOP_TOP_BUYERS_DAILY_TAG_KEY, shopId, day);
                    redisCache.incrementScoreForSortedSet(dailyKey, String.valueOf(body.getUserId()), 1.0);
                    Long ttl = redisCache.getExpire(dailyKey, TimeUnit.SECONDS);
                    if (ttl == null || ttl < 0) {
                        redisCache.expire(dailyKey, 90, TimeUnit.DAYS);
                    }
                }
            } catch (Exception e) {
                log.warn("统计店铺Top买家失败，忽略不影响主流程", e);
            }
        });
    }

    @Override
    protected void afterConsumeFailure(MessageExtend<SeckillVoucherMessage> message, Throwable throwable) {
        super.afterConsumeFailure(message, throwable);
        SeckillVoucherOrderOperate operate = SeckillVoucherOrderOperate.YES;
        if (throwable instanceof HmdpFrameException hfe
                && Objects.nonNull(hfe.getCode())
                && hfe.getCode().equals(org.javaup.enums.BaseCode.VOUCHER_ORDER_EXIST.getCode())) {
            operate = SeckillVoucherOrderOperate.NO;
        }
        SeckillVoucherMessage body = message.getMessageBody();
        long traceId = snowflakeIdGenerator.nextId();
        redisVoucherData.rollbackRedisVoucherData(
                operate, traceId,
                body.getVoucherId(), body.getUserId(), body.getOrderId(),
                body.getAfterQty(), body.getChangeQty(), body.getBeforeQty()
        );
        try {
            String detail = throwable == null ? "consume failed" : "consume failed: " + throwable.getMessage();
            voucherReconcileLogService.saveReconcileLog(
                    LogType.RESTORE.getCode(), BusinessType.FAIL.getCode(), detail, traceId, message);
        } catch (Exception e) {
            log.warn("保存对账日志失败(消费失败)", e);
        }
    }

    /**
     * 从 Redis 查询优惠券对应的 shopId（shop-service 或 voucher-service 负责写入缓存）
     * 若未命中缓存则返回 0L（Top买家榜单会写入 key=0 分区，不影响主流程）
     */
    private Long resolveShopId(Long voucherId) {
        try {
            Object cached = redisCache.get(
                    RedisKeyBuild.createRedisKey(RedisKeyManage.SECKILL_VOUCHER_TAG_KEY, voucherId),
                    Object.class
            );
            if (cached instanceof Map<?, ?> map) {
                Object shopId = map.get("shopId");
                if (shopId != null) {
                    return Long.parseLong(String.valueOf(shopId));
                }
            }
        } catch (Exception e) {
            log.warn("resolveShopId 失败，voucherId={}", voucherId);
        }
        return null;
    }
}
