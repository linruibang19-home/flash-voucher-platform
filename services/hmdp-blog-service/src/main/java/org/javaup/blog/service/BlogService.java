package org.javaup.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.javaup.blog.entity.Blog;
import org.javaup.dto.Result;

public interface BlogService extends IService<Blog> {

    Result<?> queryHotBlog(Long currentUserId, Integer current);

    Result<?> queryBlogById(Long currentUserId, Long id);

    Result<?> likeBlog(Long currentUserId, Long id);

    Result<?> queryBlogLikes(Long id);

    Result<?> saveBlog(Long currentUserId, Blog blog);

    Result<?> queryBlogOfFollow(Long currentUserId, Long max, Integer offset);
}
