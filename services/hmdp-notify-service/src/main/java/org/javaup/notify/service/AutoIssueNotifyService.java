package org.javaup.notify.service;

/**
 * @description: 自动发券通知接口
 * @maintainer: lrb
 **/
public interface AutoIssueNotifyService {

    /**
     * 发送自动发券通知
     *
     * @param voucherId          优惠券ID
     * @param userId             被通知用户ID
     * @param subscribeBeginTime 用户订阅开始时间戳（毫秒）
     */
    void sendAutoIssueNotify(Long voucherId, Long userId, Long orderId);
}
