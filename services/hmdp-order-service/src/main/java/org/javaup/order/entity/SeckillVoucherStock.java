package org.javaup.order.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @description: 秒杀券库存快照（仅供 order-service 读取 stock 字段，不做完整业务管理）
 *               在共享数据库阶段直接访问 tb_seckill_voucher；
 *               未来 DB 拆分后替换为 Feign 调用 voucher-service。
 * @maintainer: lrb
 **/
@Data
@TableName("tb_seckill_voucher")
public class SeckillVoucherStock {

    @TableId(value = "voucher_id")
    private Long voucherId;

    private Integer stock;
}
