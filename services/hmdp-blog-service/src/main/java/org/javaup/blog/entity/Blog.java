package org.javaup.blog.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("tb_blog")
public class Blog {

    @TableId(value = "id")
    private Long id;

    private Long shopId;

    private Long userId;

    @TableField(exist = false)
    private String icon;

    @TableField(exist = false)
    private String name;

    @TableField(exist = false)
    private Boolean isLike;

    private String title;

    private String images;

    private String content;

    private Integer liked;

    private Integer comments;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
