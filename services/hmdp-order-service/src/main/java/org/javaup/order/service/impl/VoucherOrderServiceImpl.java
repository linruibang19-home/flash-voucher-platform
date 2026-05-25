package org.javaup.order.service.impl;

import cn.hutool.core.date.LocalDateTimeUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.javaup.api.voucher.VoucherClient;
import org.javaup.enums.BaseCode;
import org.javaup.core.RedisKeyManage;
import org.javaup.dto.CancelVoucherOrderDto;
import org.javaup.dto.GetVoucherOrderByVoucherIdDto;
import org.javaup.dto.GetVoucherOrderDto;
import org.javaup.dto.VoucherReconcileLogDto;
import org.javaup.enums.BusinessType;
import org.javaup.enums.LogType;
import org.javaup.enums.OrderStatus;
import org.javaup.enums.SeckillVoucherOrderOperate;
import org.javaup.exception.HmdpFrameException;
import org.javaup.message.SeckillVoucherMessage;
import org.javaup.message.MessageExtend;
import org.javaup.order.entity.SeckillVoucherStock;
import org.javaup.order.entity.VoucherOrder;
import org.javaup.order.mapper.SeckillVoucherStockMapper;
import org.javaup.order.mapper.VoucherOrderMapper;
import org.javaup.order.service.IVoucherOrderService;
import org.javaup.order.service.IVoucherReconcileLogService;
import org.javaup.redis.RedisCache;
import org.javaup.redis.RedisKeyBuild;
import org.javaup.order.redis.RedisVoucherData;
import org.javaup.repeatexecutelimit.annotion.RepeatExecuteLimit;
import org.javaup.toolkit.SnowflakeIdGenerator;
import org.javaup.order.utils.UserHolder;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static org.javaup.constant.RepeatExecuteLimitConstants.SECKILL_VOUCHER_ORDER;

/**
 * @description: 优惠券订单服务实现（order-service 侧）
 * @maintainer: lrb
 **/
@Slf4j
@Service
public class VoucherOrderServiceImpl extends ServiceImpl<VoucherOrderMapper, VoucherOrder>
        implements IVoucherOrderService {

    @Resource
    private IVoucherReconcileLogService voucherReconcileLogService;

    @Resource
    private SeckillVoucherStockMapper seckillVoucherStockMapper;

    @Resource
    private RedisCache redisCache;

    @Resource
    private RedisVoucherData redisVoucherData;

    @Resource
    private SnowflakeIdGenerator snowflakeIdGenerator;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private VoucherClient voucherClient;

    @Override
    @RepeatExecuteLimit(name = SECKILL_VOUCHER_ORDER, keys = {"#message.uuid"})
    @Transactional(rollbackFor = Exception.class)
    public boolean createVoucherOrder(MessageExtend<SeckillVoucherMessage> message) {
        SeckillVoucherMessage body = message.getMessageBody();
        Long userId = body.getUserId();
        // 幂等：同一用户同一券不重复下单
        VoucherOrder existing = lambdaQuery()
                .eq(VoucherOrder::getVoucherId, body.getVoucherId())
                .eq(VoucherOrder::getUserId, userId)
                .eq(VoucherOrder::getStatus, OrderStatus.NORMAL.getCode())
                .one();
        if (Objects.nonNull(existing)) {
            log.warn("已存在此订单，voucherId={}，userId={}", body.getVoucherId(), userId);
            throw new HmdpFrameException(BaseCode.VOUCHER_ORDER_EXIST);
        }
        // 扣减 DB 库存
        int updated = seckillVoucherStockMapper.deductStock(body.getVoucherId());
        if (updated == 0) {
            throw new HmdpFrameException("优惠券库存不足！voucherId=" + body.getVoucherId());
        }
        VoucherOrder order = new VoucherOrder();
        order.setId(body.getOrderId());
        order.setUserId(body.getUserId());
        order.setVoucherId(body.getVoucherId());
        order.setCreateTime(LocalDateTimeUtil.now());
        save(order);
        redisCache.set(
                RedisKeyBuild.createRedisKey(RedisKeyManage.DB_SECKILL_ORDER_KEY, body.getOrderId()),
                order,
                60,
                TimeUnit.SECONDS
        );
        voucherReconcileLogService.saveReconcileLog(
                LogType.DEDUCT.getCode(),
                BusinessType.SUCCESS.getCode(),
                "order created",
                message
        );
        return true;
    }

    @Override
    public Long getSeckillVoucherOrder(GetVoucherOrderDto dto) {
        VoucherOrder order = redisCache.get(
                RedisKeyBuild.createRedisKey(RedisKeyManage.DB_SECKILL_ORDER_KEY, dto.getOrderId()),
                VoucherOrder.class
        );
        if (Objects.nonNull(order)) {
            return order.getId();
        }
        order = getById(dto.getOrderId());
        return Objects.nonNull(order) ? order.getId() : null;
    }

    @Override
    public Long getSeckillVoucherOrderIdByVoucherId(GetVoucherOrderByVoucherIdDto dto) {
        VoucherOrder order = lambdaQuery()
                .eq(VoucherOrder::getUserId, UserHolder.getUser().getId())
                .eq(VoucherOrder::getVoucherId, dto.getVoucherId())
                .eq(VoucherOrder::getStatus, OrderStatus.NORMAL.getCode())
                .one();
        return Objects.nonNull(order) ? order.getId() : null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean cancel(CancelVoucherOrderDto dto) {
        Long currentUserId = UserHolder.getUser().getId();
        VoucherOrder order = lambdaQuery()
                .eq(VoucherOrder::getUserId, currentUserId)
                .eq(VoucherOrder::getVoucherId, dto.getVoucherId())
                .eq(VoucherOrder::getStatus, OrderStatus.NORMAL.getCode())
                .one();
        if (Objects.isNull(order)) {
            throw new HmdpFrameException(BaseCode.SECKILL_VOUCHER_ORDER_NOT_EXIST);
        }
        SeckillVoucherStock stock = seckillVoucherStockMapper.selectById(dto.getVoucherId());
        if (Objects.isNull(stock)) {
            throw new HmdpFrameException(BaseCode.SECKILL_VOUCHER_NOT_EXIST);
        }
        int currentStock = stock.getStock();
        boolean updateResult = lambdaUpdate()
                .set(VoucherOrder::getStatus, OrderStatus.CANCEL.getCode())
                .set(VoucherOrder::getUpdateTime, LocalDateTime.now())
                .eq(VoucherOrder::getUserId, currentUserId)
                .eq(VoucherOrder::getVoucherId, dto.getVoucherId())
                .update();
        long traceId = snowflakeIdGenerator.nextId();
        VoucherReconcileLogDto logDto = new VoucherReconcileLogDto();
        logDto.setOrderId(order.getId());
        logDto.setUserId(order.getUserId());
        logDto.setVoucherId(order.getVoucherId());
        logDto.setDetail("cancel voucher order");
        logDto.setBeforeQty(currentStock);
        logDto.setChangeQty(1);
        logDto.setAfterQty(currentStock + 1);
        logDto.setTraceId(traceId);
        logDto.setLogType(LogType.RESTORE.getCode());
        logDto.setBusinessType(BusinessType.CANCEL.getCode());
        boolean saveLogResult = voucherReconcileLogService.saveReconcileLog(logDto);
        boolean rollbackStockResult = seckillVoucherStockMapper.rollbackStock(dto.getVoucherId()) > 0;
        boolean result = updateResult && saveLogResult && rollbackStockResult;
        if (result) {
            redisVoucherData.rollbackRedisVoucherData(
                    SeckillVoucherOrderOperate.YES,
                    traceId,
                    order.getVoucherId(),
                    order.getUserId(),
                    order.getId(),
                    currentStock,
                    1,
                    currentStock + 1
            );
            redisCache.delForHash(
                    RedisKeyBuild.createRedisKey(RedisKeyManage.SECKILL_SUBSCRIBE_STATUS_TAG_KEY, dto.getVoucherId()),
                    String.valueOf(order.getUserId())
            );
            // 更新商店每日 Top 买家榜（-1 分）
            Long shopId = fetchShopId(order.getVoucherId());
            if (shopId != null) {
                String day = order.getCreateTime().format(DateTimeFormatter.BASIC_ISO_DATE);
                RedisKeyBuild dailyKey = RedisKeyBuild.createRedisKey(
                        RedisKeyManage.SECKILL_SHOP_TOP_BUYERS_DAILY_TAG_KEY, shopId, day);
                redisCache.incrementScoreForSortedSet(dailyKey, String.valueOf(order.getUserId()), -1.0);
            }
            // 最优候选用户自动发券（best-effort）
            try {
                voucherClient.autoIssueToEarliestSubscriber(order.getVoucherId(), order.getUserId());
            } catch (Exception e) {
                log.warn("自动发券 Feign 调用失败，voucherId={}, err={}", order.getVoucherId(), e.getMessage());
            }
        }
        return result;
    }

    /**
     * 查询 tb_voucher 获取 shopId（供 cancel 更新商店每日 Top 买家榜）
     * 返回 null 时跳过榜单更新
     */
    private Long fetchShopId(Long voucherId) {
        try {
            String shopIdStr = stringRedisTemplate.opsForValue()
                    .get("voucher:shop:" + voucherId);
            if (shopIdStr != null) {
                return Long.parseLong(shopIdStr);
            }
        } catch (Exception ignore) {
        }
        return null;
    }
}
