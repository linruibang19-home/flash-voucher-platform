package org.javaup.order.controller;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.javaup.dto.CancelVoucherOrderDto;
import org.javaup.dto.GetVoucherOrderByVoucherIdDto;
import org.javaup.dto.GetVoucherOrderDto;
import org.javaup.dto.Result;
import org.javaup.order.service.IVoucherOrderService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @description: 优惠券订单 API（order-service 侧）
 *               负责：订单查询、订单取消
 *               秒杀下单入口保留在 hmdp-voucher-service
 * @maintainer: lrb
 **/
@RestController
@RequestMapping("/voucher-order")
public class VoucherOrderController {

    @Resource
    private IVoucherOrderService voucherOrderService;

    /**
     * 按订单ID查询订单
     */
    @PostMapping("/get/seckill/voucher/order-id")
    public Result<Long> getSeckillVoucherOrder(@Valid @RequestBody GetVoucherOrderDto dto) {
        return Result.ok(voucherOrderService.getSeckillVoucherOrder(dto));
    }

    /**
     * 按优惠券ID查询当前用户订单ID
     */
    @PostMapping("/get/seckill/voucher/order-id/by/voucher-id")
    public Result<Long> getSeckillVoucherOrderIdByVoucherId(@Valid @RequestBody GetVoucherOrderByVoucherIdDto dto) {
        return Result.ok(voucherOrderService.getSeckillVoucherOrderIdByVoucherId(dto));
    }

    /**
     * 取消订单
     */
    @PostMapping("/cancel")
    public Result<Boolean> cancel(@Valid @RequestBody CancelVoucherOrderDto dto) {
        return Result.ok(voucherOrderService.cancel(dto));
    }
}
