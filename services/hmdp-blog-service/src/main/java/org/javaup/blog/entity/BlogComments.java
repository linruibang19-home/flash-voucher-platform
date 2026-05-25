package org.javaup.blog.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("tb_blog_comments")
public class BlogComments {

    @TableId(value = "id")
    private Long id;

    private Long userId;

    private Long blogId;

    private Long parentId;

    private Long answerId;

    private String content;

    private Integer liked;

    private Boolean status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
