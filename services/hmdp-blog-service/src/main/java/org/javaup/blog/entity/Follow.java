package org.javaup.blog.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("tb_follow")
public class Follow {

    @TableId(value = "id")
    private Long id;

    private Long userId;

    private Long followUserId;

    private LocalDateTime createTime;
}
