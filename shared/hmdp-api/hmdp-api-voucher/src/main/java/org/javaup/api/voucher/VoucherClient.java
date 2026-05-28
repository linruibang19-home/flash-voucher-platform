package org.javaup.api.voucher;

import org.javaup.dto.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @description: hmdp-voucher-service 的 Feign 客户端契约
 *               消费方通过 @EnableFeignClients(basePackages = "org.javaup.api.voucher") 启用
 * @maintainer: lrb
 **/
@FeignClient(name = "hmdp-voucher-service", url = "${hmdp.voucher-service.url:}")
public interface VoucherClient {

    /**
     * 订单取消后，向订阅列表中最早的候选用户自动发券
     *
     * @param voucherId     优惠券ID
     * @param excludeUserId 排除的用户ID（取消订单的用户）
     */
    @PostMapping("/voucher/seckill/auto-issue")
    Result<Boolean> autoIssueToEarliestSubscriber(@RequestParam("voucherId") Long voucherId,
                                                   @RequestParam("excludeUserId") Long excludeUserId);

    /**
     * 内部接口：查询全量秒杀券 ID 列表（供 job-service 对账遍历）
     */
    @GetMapping("/voucher/internal/seckill/ids")
    Result<List<Long>> listAllSeckillVoucherIds();
}
