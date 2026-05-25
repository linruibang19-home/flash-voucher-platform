package org.javaup.api.notify;

import org.javaup.dto.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @description: hmdp-notify-service 的 Feign 客户端契约
 * @maintainer: lrb
 **/
@FeignClient(name = "hmdp-notify-service", url = "${hmdp.notify-service.url:http://localhost:8087}")
public interface NotifyClient {

    /**
     * 发送自动发券通知
     *
     * @param voucherId 优惠券ID
     * @param userId    用户ID
     * @param orderId   订单ID
     */
    @PostMapping("/notify/auto-issue")
    Result<Void> sendAutoIssueNotify(@RequestParam("voucherId") Long voucherId,
                                     @RequestParam("userId") Long userId,
                                     @RequestParam("orderId") Long orderId);

    /**
     * 发送回滚失败告警通知
     *
     * @param voucherId     优惠券ID
     * @param userId        用户ID
     * @param orderId       订单ID
     * @param traceId       追踪ID
     * @param retryAttempts 重试次数
     * @param source        来源组件
     * @param detail        失败详情
     */
    @PostMapping("/notify/rollback-alert")
    Result<Void> sendRollbackAlert(@RequestParam("voucherId") Long voucherId,
                                   @RequestParam("userId") Long userId,
                                   @RequestParam("orderId") Long orderId,
                                   @RequestParam("traceId") Long traceId,
                                   @RequestParam("retryAttempts") Integer retryAttempts,
                                   @RequestParam("source") String source,
                                   @RequestParam("detail") String detail);
}
