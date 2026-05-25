package org.javaup.notify.service.impl;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.javaup.core.RedisKeyManage;
import org.javaup.notify.service.RollbackAlertService;
import org.javaup.redis.RedisCache;
import org.javaup.redis.RedisKeyBuild;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * @description: 回滚失败告警通知实现（当前为 stub，生产环境替换为真实 SMS/Email SDK）
 * @maintainer: lrb
 **/
@Slf4j
@Service
public class RollbackAlertServiceImpl implements RollbackAlertService {

    @Value("${seckill.rollback.alert.sms.enabled:false}")
    private boolean smsEnabled;

    @Value("${seckill.rollback.alert.email.enabled:false}")
    private boolean emailEnabled;

    @Value("${seckill.rollback.alert.sms.to:}")
    private String smsTo;

    @Value("${seckill.rollback.alert.email.to:}")
    private String emailTo;

    @Value("${seckill.rollback.alert.dedup.window-seconds:300}")
    private long dedupWindowSeconds;

    @Resource
    private RedisCache redisCache;

    @Override
    public void sendRollbackAlert(Long voucherId, Long userId, Long orderId, Long traceId,
                                  Integer retryAttempts, String source, String detail) {
        try {
            if (!shouldNotify(voucherId)) {
                return;
            }
            String content = String.format(
                    "回滚失败告警 | voucherId=%s userId=%s orderId=%s traceId=%s attempts=%s source=%s detail=%s",
                    voucherId, userId, orderId, traceId, retryAttempts, source, detail);
            if (smsEnabled && smsTo != null && !smsTo.isEmpty()) {
                log.warn("[ROLLBACK_SMS] to={} content={}", smsTo, content);
            }
            if (emailEnabled && emailTo != null && !emailTo.isEmpty()) {
                log.warn("[ROLLBACK_EMAIL] to={} content={}", emailTo, content);
            }
            if (!smsEnabled && !emailEnabled) {
                log.warn("[ROLLBACK_ALERT] {}", content);
            }
        } catch (Exception e) {
            log.warn("[ROLLBACK_ALERT] 发送异常", e);
        }
    }

    private boolean shouldNotify(Long voucherId) {
        try {
            return redisCache.setIfAbsent(
                    RedisKeyBuild.createRedisKey(RedisKeyManage.SECKILL_ROLLBACK_ALERT_DEDUP_KEY, voucherId),
                    "1",
                    dedupWindowSeconds,
                    TimeUnit.SECONDS
            );
        } catch (Exception e) {
            return true;
        }
    }
}
