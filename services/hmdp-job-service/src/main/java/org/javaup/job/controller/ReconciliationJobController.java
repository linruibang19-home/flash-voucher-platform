package org.javaup.job.controller;

import jakarta.annotation.Resource;
import org.javaup.dto.Result;
import org.javaup.job.service.ReconciliationJobService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @description: 对账任务手动触发接口
 * @maintainer: lrb
 **/
@RestController
@RequestMapping("/job/reconciliation")
public class ReconciliationJobController {

    @Resource
    private ReconciliationJobService reconciliationJobService;

    /**
     * 手动触发全量对账
     */
    @PostMapping("/all")
    public Result<Void> triggerAll() {
        reconciliationJobService.executeAll();
        return Result.ok();
    }

    /**
     * 手动触发单券对账
     */
    @PostMapping("/by-voucher")
    public Result<Void> triggerByVoucher(@RequestParam("voucherId") Long voucherId) {
        reconciliationJobService.executeByVoucherId(voucherId);
        return Result.ok();
    }
}
