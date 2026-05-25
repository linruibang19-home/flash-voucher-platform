package org.javaup.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import org.javaup.order.entity.SeckillVoucherStock;

/**
 * @description: 秒杀券库存操作 Mapper（order-service 在共享 DB 阶段对 tb_seckill_voucher 的最小操作集）
 *               仅包含订单创建/回滚所需的 stock 字段变更，不做完整 CRUD。
 * @maintainer: lrb
 **/
public interface SeckillVoucherStockMapper extends BaseMapper<SeckillVoucherStock> {

    /**
     * CAS 扣减库存（stock > 0 时 stock - 1）
     *
     * @param voucherId 优惠券ID
     * @return 影响行数，0 表示库存不足
     */
    @Update("UPDATE tb_seckill_voucher SET stock = stock - 1, update_time = NOW() WHERE voucher_id = #{voucherId} AND stock > 0")
    int deductStock(@Param("voucherId") Long voucherId);

    /**
     * 回滚库存（stock + 1）
     *
     * @param voucherId 优惠券ID
     * @return 影响行数
     */
    @Update("UPDATE tb_seckill_voucher SET stock = stock + 1, update_time = NOW() WHERE voucher_id = #{voucherId}")
    int rollbackStock(@Param("voucherId") Long voucherId);
}
