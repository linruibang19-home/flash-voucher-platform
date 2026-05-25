package org.javaup.notify.service;

/**
 * @description: 回滚失败告警通知接口
 * @maintainer: lrb
 **/
public interface RollbackAlertService {

    void sendRollbackAlert(Long voucherId, Long userId, Long orderId, Long traceId,
                           Integer retryAttempts, String source, String detail);
}
