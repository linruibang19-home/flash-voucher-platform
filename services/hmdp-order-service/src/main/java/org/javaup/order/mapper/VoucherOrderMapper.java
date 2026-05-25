package org.javaup.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.javaup.order.entity.VoucherOrder;

/**
 * @description: 优惠券订单 Mapper
 * @maintainer: lrb
 **/
public interface VoucherOrderMapper extends BaseMapper<VoucherOrder> {

    /**
     * 根据优惠券ID和用户ID删除订单（测试/运维用）
     */
    @Delete("DELETE FROM tb_voucher_order WHERE voucher_id = #{voucherId} AND user_id = #{userId}")
    Integer deleteVoucherOrder(@Param("voucherId") Long voucherId, @Param("userId") Long userId);
}
