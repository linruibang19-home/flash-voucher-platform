package org.javaup.notify.controller;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.javaup.dto.Result;
import org.javaup.notify.service.AutoIssueNotifyService;
import org.javaup.notify.service.RollbackAlertService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @description: 通知服务对外接口（内部调用，不经过 Gateway 认证）
 * @maintainer: lrb
 **/
@Slf4j
@RestController
@RequestMapping("/notify")
public class NotifyController {

    @Resource
    private AutoIssueNotifyService autoIssueNotifyService;

    @Resource
    private RollbackAlertService rollbackAlertService;

    /**
     * 发送自动发券通知
     */
    @PostMapping("/auto-issue")
    public Result<Void> sendAutoIssueNotify(@RequestParam("voucherId") Long voucherId,
                                             @RequestParam("userId") Long userId,
                                             @RequestParam("orderId") Long orderId) {
        autoIssueNotifyService.sendAutoIssueNotify(voucherId, userId, orderId);
        return Result.ok();
    }

    /**
     * 发送回滚失败告警
     */
    @PostMapping("/rollback-alert")
    public Result<Void> sendRollbackAlert(@RequestParam("voucherId") Long voucherId,
                                           @RequestParam("userId") Long userId,
                                           @RequestParam("orderId") Long orderId,
                                           @RequestParam("traceId") Long traceId,
                                           @RequestParam("retryAttempts") Integer retryAttempts,
                                           @RequestParam("source") String source,
                                           @RequestParam("detail") String detail) {
        rollbackAlertService.sendRollbackAlert(voucherId, userId, orderId, traceId, retryAttempts, source, detail);
        return Result.ok();
    }
}
