package org.javaup.order.service;

import org.javaup.order.entity.RollbackFailureLog;

/**
 * @description: 回滚失败告警接口（调用 hmdp-notify-service）
 * @maintainer: lrb
 **/
public interface IRollbackAlertService {

    void sendRollbackAlert(RollbackFailureLog log);
}
