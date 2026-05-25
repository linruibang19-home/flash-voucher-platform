package org.javaup.job.service;

/**
 * @description: 对账定时任务服务接口
 * @maintainer: lrb
 **/
public interface ReconciliationJobService {

    /**
     * 对所有秒杀券执行一次全量对账
     */
    void executeAll();

    /**
     * 对指定优惠券执行对账
     */
    void executeByVoucherId(Long voucherId);

    /**
     * 删除指定优惠券的 Redis 库存（加锁保护）
     */
    void delRedisStock(Long voucherId);
}
