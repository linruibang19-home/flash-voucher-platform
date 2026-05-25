package org.javaup.job.schedule;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.javaup.job.service.ReconciliationJobService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @description: 对账定时任务调度器
 *               默认每 5 分钟执行一次全量对账；可通过 HTTP 接口手动触发
 * @maintainer: lrb
 **/
@Slf4j
@Component
public class ReconciliationScheduler {

    @Resource
    private ReconciliationJobService reconciliationJobService;

    /**
     * 定时全量对账（每 5 分钟）
     * cron 表达式可通过配置覆盖
     */
    @Scheduled(cron = "${hmdp.job.reconciliation.cron:0 0/5 * * * ?}")
    public void scheduledReconciliationAll() {
        try {
            log.info("[ReconciliationScheduler] 触发定时全量对账");
            reconciliationJobService.executeAll();
        } catch (Exception e) {
            log.error("[ReconciliationScheduler] 定时对账异常", e);
        }
    }
}
