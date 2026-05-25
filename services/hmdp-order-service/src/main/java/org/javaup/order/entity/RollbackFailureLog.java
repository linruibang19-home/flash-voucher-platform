package org.javaup.order.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @description: 回滚失败日志
 * @maintainer: lrb
 **/
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("tb_rollback_failure_log")
public class RollbackFailureLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id")
    private Long id;

    private Long voucherId;

    private Long userId;

    private Long orderId;

    private Long traceId;

    private String detail;

    private Integer resultCode;

    private Integer retryAttempts;

    private String source;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
