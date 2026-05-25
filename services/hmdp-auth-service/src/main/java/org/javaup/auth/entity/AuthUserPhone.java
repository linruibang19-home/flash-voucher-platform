package org.javaup.auth.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("tb_user_phone")
public class AuthUserPhone implements Serializable {

    @TableId(value = "id")
    private Long id;
    private Long userId;
    private String phone;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
