package org.javaup.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.javaup.dto.CancelVoucherOrderDto;
import org.javaup.dto.GetVoucherOrderByVoucherIdDto;
import org.javaup.dto.GetVoucherOrderDto;
import org.javaup.message.SeckillVoucherMessage;
import org.javaup.message.MessageExtend;
import org.javaup.order.entity.VoucherOrder;

/**
 * @description: 优惠券订单服务接口（order-service 侧）
 * @maintainer: lrb
 **/
public interface IVoucherOrderService extends IService<VoucherOrder> {

    /**
     * 从 Kafka 消息创建订单（幂等，通过 @RepeatExecuteLimit 防重）
     */
    boolean createVoucherOrder(MessageExtend<SeckillVoucherMessage> message);

    /**
     * 按订单ID查询订单（先查 Redis 缓存，再查 DB）
     */
    Long getSeckillVoucherOrder(GetVoucherOrderDto getVoucherOrderDto);

    /**
     * 按优惠券ID查询当前用户的正常状态订单ID
     */
    Long getSeckillVoucherOrderIdByVoucherId(GetVoucherOrderByVoucherIdDto getVoucherOrderByVoucherIdDto);

    /**
     * 取消订单：更新状态、回滚库存、回滚 Redis、触发自动发券
     */
    Boolean cancel(CancelVoucherOrderDto cancelVoucherOrderDto);
}
