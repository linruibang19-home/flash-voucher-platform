package org.javaup.notify.service.impl;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.javaup.core.RedisKeyManage;
import org.javaup.notify.service.AutoIssueNotifyService;
import org.javaup.redis.RedisCache;
import org.javaup.redis.RedisKeyBuild;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * @description: 自动发券通知服务实现（当前为 stub，生产环境替换为真实 SMS/Push SDK）
 * @maintainer: lrb
 **/
@Slf4j
@Service
public class AutoIssueNotifyServiceImpl implements AutoIssueNotifyService {

    @Value("${seckill.notify.auto-issue.sms.enabled:false}")
    private boolean smsEnabled;

    @Value("${seckill.notify.auto-issue.app.enabled:false}")
    private boolean appEnabled;

    @Value("${seckill.notify.auto-issue.sms.to:}")
    private String smsTo;

    @Value("${seckill.notify.auto-issue.dedup.window-seconds:1800}")
    private long dedupWindowSeconds;

    @Resource
    private RedisCache redisCache;

    @Override
    public void sendAutoIssueNotify(Long voucherId, Long userId, Long orderId) {
        try {
            if (!shouldNotify(voucherId, userId)) {
                return;
            }
            String content = String.format("自动发券成功 | voucherId=%s userId=%s orderId=%s", voucherId, userId, orderId);
            if (smsEnabled && smsTo != null && !smsTo.isEmpty()) {
                log.info("[AUTO_ISSUE_SMS] to={} content={}", smsTo, content);
            }
            if (appEnabled) {
                log.info("[AUTO_ISSUE_APP] userId={} content={}", userId, content);
            }
            if (!smsEnabled && !appEnabled) {
                log.info("[AUTO_ISSUE_NOTIFY] voucherId={} userId={} content={}", voucherId, userId, content);
            }
        } catch (Exception e) {
            log.warn("[AUTO_ISSUE_NOTIFY] 发送失败 voucherId={} userId={}", voucherId, userId, e);
        }
    }

    private boolean shouldNotify(Long voucherId, Long userId) {
        try {
            return redisCache.setIfAbsent(
                    RedisKeyBuild.createRedisKey(RedisKeyManage.SECKILL_AUTO_ISSUE_NOTIFY_DEDUP_KEY, voucherId, userId),
                    "1",
                    dedupWindowSeconds,
                    TimeUnit.SECONDS
            );
        } catch (Exception e) {
            return true;
        }
    }
}
