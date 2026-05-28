package org.javaup.api.order;

import org.javaup.dto.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

/**
 * @description: hmdp-order-service 的 Feign 客户端契约
 *               供 hmdp-job-service 调用
 * @maintainer: lrb
 **/
@FeignClient(name = "hmdp-order-service", url = "${hmdp.order-service.url:}")
public interface OrderClient {

    /**
     * 查询指定优惠券下对账状态为 PENDING 的订单 ID 列表
     */
    @GetMapping("/internal/voucher-order/pending-ids")
    Result<List<Long>> listPendingOrderIds(@RequestParam("voucherId") Long voucherId);

    /**
     * 批量标记订单对账状态
     *
     * @param orderIds           订单 ID 列表（逗号分隔字符串）
     * @param reconciliationStatus 目标对账状态码
     */
    @PostMapping("/internal/voucher-order/mark-reconciliation")
    Result<Void> markReconciliationStatus(@RequestParam("orderIds") String orderIds,
                                           @RequestParam("reconciliationStatus") Integer reconciliationStatus);

    /**
     * 查询指定订单的对账日志（返回 Map<traceId, logType> 结构）
     */
    @GetMapping("/internal/voucher-order/reconcile-logs")
    Result<Map<String, Integer>> getReconcileLogsByOrderId(@RequestParam("orderId") Long orderId);
}
