package org.javaup.blog.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("tb_user")
public class User {

    @TableId(value = "id")
    private Long id;

    private String phone;

    private String nickName;

    private String icon;
}
