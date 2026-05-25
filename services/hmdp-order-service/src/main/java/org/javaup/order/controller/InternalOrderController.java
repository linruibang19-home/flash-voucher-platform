package org.javaup.order.controller;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import jakarta.annotation.Resource;
import org.javaup.dto.Result;
import org.javaup.enums.ReconciliationStatus;
import org.javaup.order.entity.VoucherOrder;
import org.javaup.order.entity.VoucherReconcileLog;
import org.javaup.order.service.IVoucherOrderService;
import org.javaup.order.service.IVoucherReconcileLogService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @description: order-service 内部接口（供 hmdp-job-service 调用，不暴露给外部）
 * @maintainer: lrb
 **/
@RestController
@RequestMapping("/internal/voucher-order")
public class InternalOrderController {

    @Resource
    private IVoucherOrderService voucherOrderService;

    @Resource
    private IVoucherReconcileLogService voucherReconcileLogService;

    /**
     * 查询指定优惠券下对账状态为 PENDING 的订单 ID 列表
     */
    @GetMapping("/pending-ids")
    public Result<List<Long>> listPendingOrderIds(@RequestParam("voucherId") Long voucherId) {
        List<Long> ids = voucherOrderService.lambdaQuery()
                .eq(VoucherOrder::getVoucherId, voucherId)
                .eq(VoucherOrder::getReconciliationStatus, ReconciliationStatus.PENDING.getCode())
                .list()
                .stream()
                .map(VoucherOrder::getId)
                .toList();
        return Result.ok(ids);
    }

    /**
     * 批量标记订单及其对账日志的对账状态
     */
    @PostMapping("/mark-reconciliation")
    public Result<Void> markReconciliationStatus(@RequestParam("orderIds") String orderIds,
                                                  @RequestParam("reconciliationStatus") Integer reconciliationStatus) {
        List<Long> ids = Arrays.stream(orderIds.split(","))
                .map(String::trim)
                .map(Long::parseLong)
                .toList();
        voucherOrderService.update(new LambdaUpdateWrapper<VoucherOrder>()
                .in(VoucherOrder::getId, ids)
                .set(VoucherOrder::getReconciliationStatus, reconciliationStatus)
                .set(VoucherOrder::getUpdateTime, LocalDateTime.now()));
        voucherReconcileLogService.update(new LambdaUpdateWrapper<VoucherReconcileLog>()
                .in(VoucherReconcileLog::getOrderId, ids)
                .set(VoucherReconcileLog::getReconciliationStatus, reconciliationStatus)
                .set(VoucherReconcileLog::getUpdateTime, LocalDateTime.now()));
        return Result.ok();
    }

    /**
     * 查询指定订单的对账日志（key=traceId, value=logType）
     */
    @GetMapping("/reconcile-logs")
    public Result<Map<String, Integer>> getReconcileLogsByOrderId(@RequestParam("orderId") Long orderId) {
        Map<String, Integer> map = voucherReconcileLogService.lambdaQuery()
                .eq(VoucherReconcileLog::getOrderId, orderId)
                .list()
                .stream()
                .collect(Collectors.toMap(
                        log -> String.valueOf(log.getTraceId()),
                        VoucherReconcileLog::getLogType,
                        (a, b) -> a
                ));
        return Result.ok(map);
    }
}
