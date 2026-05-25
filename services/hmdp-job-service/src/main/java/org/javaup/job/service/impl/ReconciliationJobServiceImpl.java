package org.javaup.job.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.javaup.api.order.OrderClient;
import org.javaup.api.voucher.VoucherClient;
import org.javaup.core.RedisKeyManage;
import org.javaup.dto.Result;
import org.javaup.enums.ReconciliationStatus;
import org.javaup.job.service.ReconciliationJobService;
import org.javaup.redis.RedisCache;
import org.javaup.redis.RedisKeyBuild;
import org.javaup.servicelock.LockType;
import org.javaup.servicelock.annotion.ServiceLock;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static org.javaup.constant.DistributedLockConstants.UPDATE_SECKILL_VOUCHER_STOCK_LOCK;

/**
 * @description: 对账定时任务服务实现
 *               通过 Feign 调用 voucher-service 和 order-service 完成跨服务对账
 * @maintainer: lrb
 **/
@Slf4j
@Service
public class ReconciliationJobServiceImpl implements ReconciliationJobService {

    @Resource
    private VoucherClient voucherClient;

    @Resource
    private OrderClient orderClient;

    @Resource
    private RedisCache redisCache;

    @Override
    public void executeAll() {
        log.info("[ReconciliationJob] 开始全量对账");
        Result<List<Long>> voucherIdsResult = voucherClient.listAllSeckillVoucherIds();
        if (voucherIdsResult == null || !voucherIdsResult.getSuccess()
                || CollectionUtil.isEmpty(voucherIdsResult.getData())) {
            log.warn("[ReconciliationJob] 未查询到任何秒杀券，对账结束");
            return;
        }
        for (Long voucherId : voucherIdsResult.getData()) {
            try {
                executeByVoucherId(voucherId);
            } catch (Exception e) {
                log.error("[ReconciliationJob] 对账失败 voucherId={}", voucherId, e);
            }
        }
        log.info("[ReconciliationJob] 全量对账完成");
    }

    @Override
    public void executeByVoucherId(Long voucherId) {
        Result<List<Long>> pendingResult = orderClient.listPendingOrderIds(voucherId);
        if (pendingResult == null || !pendingResult.getSuccess()
                || CollectionUtil.isEmpty(pendingResult.getData())) {
            return;
        }
        for (Long orderId : pendingResult.getData()) {
            try {
                processOrder(voucherId, orderId);
            } catch (Exception e) {
                log.error("[ReconciliationJob] 处理订单失败 voucherId={} orderId={}", voucherId, orderId, e);
            }
        }
    }

    @Override
    @ServiceLock(lockType = LockType.Write, name = UPDATE_SECKILL_VOUCHER_STOCK_LOCK, keys = {"#voucherId"})
    public void delRedisStock(Long voucherId) {
        redisCache.del(RedisKeyBuild.createRedisKey(RedisKeyManage.SECKILL_STOCK_TAG_KEY, voucherId));
    }

    private void processOrder(Long voucherId, Long orderId) {
        Result<Map<String, Integer>> logsResult = orderClient.getReconcileLogsByOrderId(orderId);
        if (logsResult == null || !logsResult.getSuccess() || CollectionUtil.isEmpty(logsResult.getData())) {
            markOrder(orderId, ReconciliationStatus.ABNORMAL);
            return;
        }
        Map<String, Integer> logs = logsResult.getData();
        int logCount = logs.size();
        if (logCount == 1 || logCount == 2) {
            markOrder(orderId, ReconciliationStatus.CONSISTENT);
        } else {
            markOrder(orderId, ReconciliationStatus.ABNORMAL);
        }
    }

    private void markOrder(Long orderId, ReconciliationStatus status) {
        try {
            orderClient.markReconciliationStatus(
                    String.valueOf(orderId),
                    status.getCode()
            );
        } catch (Exception e) {
            log.warn("[ReconciliationJob] 标记订单状态失败 orderId={} status={}", orderId, status, e);
        }
    }
}
