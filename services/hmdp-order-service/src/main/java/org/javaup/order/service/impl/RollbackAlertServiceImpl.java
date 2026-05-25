package org.javaup.order.service.impl;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.javaup.api.notify.NotifyClient;
import org.javaup.order.entity.RollbackFailureLog;
import org.javaup.order.service.IRollbackAlertService;
import org.springframework.stereotype.Service;

/**
 * @description: 回滚失败告警：通过 Feign 调用 hmdp-notify-service 发送告警通知
 * @maintainer: lrb
 **/
@Slf4j
@Service
public class RollbackAlertServiceImpl implements IRollbackAlertService {

    @Resource
    private NotifyClient notifyClient;

    @Override
    public void sendRollbackAlert(RollbackFailureLog logEntity) {
        try {
            notifyClient.sendRollbackAlert(
                    logEntity.getVoucherId(),
                    logEntity.getUserId(),
                    logEntity.getOrderId(),
                    logEntity.getTraceId(),
                    logEntity.getRetryAttempts(),
                    logEntity.getSource(),
                    logEntity.getDetail()
            );
        } catch (Exception e) {
            log.warn("[ROLLBACK_ALERT] Feign 调用 notify-service 失败，降级为本地日志输出", e);
            log.warn("[ROLLBACK_ALERT_LOCAL] voucherId={} userId={} orderId={} traceId={} source={} detail={}",
                    logEntity.getVoucherId(),
                    logEntity.getUserId(),
                    logEntity.getOrderId(),
                    logEntity.getTraceId(),
                    logEntity.getSource(),
                    logEntity.getDetail());
        }
    }
}
