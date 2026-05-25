package org.javaup.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @description: 秒杀券 Kafka 消息体（由 voucher-service 发布，order-service 消费）
 * @maintainer: lrb
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SeckillVoucherMessage {

    private Long userId;

    private Long voucherId;

    private Long orderId;

    private Long traceId;

    private Integer beforeQty;

    private Integer changeQty;

    private Integer afterQty;

    private Boolean autoIssue;
}
