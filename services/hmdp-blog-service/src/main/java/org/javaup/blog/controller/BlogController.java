package org.javaup.blog.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.javaup.blog.constant.BlogConstants;
import org.javaup.blog.entity.Blog;
import org.javaup.blog.service.BlogService;
import org.javaup.dto.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/blog")
public class BlogController {

    @Resource
    private BlogService blogService;

    @PostMapping
    public Result<?> saveBlog(@RequestHeader(value = BlogConstants.USER_ID_HEADER, required = false) Long currentUserId,
                              @RequestBody Blog blog) {
        return blogService.saveBlog(currentUserId, blog);
    }

    @PutMapping("/like/{id}")
    public Result<?> likeBlog(@RequestHeader(value = BlogConstants.USER_ID_HEADER, required = false) Long currentUserId,
                              @PathVariable("id") Long id) {
        return blogService.likeBlog(currentUserId, id);
    }

    @GetMapping("/of/me")
    public Result<?> queryMyBlog(@RequestHeader(value = BlogConstants.USER_ID_HEADER, required = false) Long currentUserId,
                                 @RequestParam(value = "current", defaultValue = "1") Integer current) {
        if (currentUserId == null) {
            return Result.fail("User is not logged in");
        }
        Page<Blog> page = blogService.query()
                .eq("user_id", currentUserId)
                .page(new Page<>(current, BlogConstants.MAX_PAGE_SIZE));
        return Result.ok(page.getRecords());
    }

    @GetMapping("/hot")
    public Result<?> queryHotBlog(@RequestHeader(value = BlogConstants.USER_ID_HEADER, required = false) Long currentUserId,
                                  @RequestParam(value = "current", defaultValue = "1") Integer current) {
        return blogService.queryHotBlog(currentUserId, current);
    }

    @GetMapping("/{id}")
    public Result<?> queryBlogById(@RequestHeader(value = BlogConstants.USER_ID_HEADER, required = false) Long currentUserId,
                                   @PathVariable("id") Long id) {
        return blogService.queryBlogById(currentUserId, id);
    }

    @GetMapping("/likes/{id}")
    public Result<?> queryBlogLikes(@PathVariable("id") Long id) {
        return blogService.queryBlogLikes(id);
    }

    @GetMapping("/of/user")
    public Result<?> queryBlogByUserId(@RequestParam(value = "current", defaultValue = "1") Integer current,
                                       @RequestParam("id") Long id) {
        Page<Blog> page = blogService.query()
                .eq("user_id", id)
                .page(new Page<>(current, BlogConstants.MAX_PAGE_SIZE));
        return Result.ok(page.getRecords());
    }

    @GetMapping("/of/follow")
    public Result<?> queryBlogOfFollow(@RequestHeader(value = BlogConstants.USER_ID_HEADER, required = false) Long currentUserId,
                                       @RequestParam("lastId") Long max,
                                       @RequestParam(value = "offset", defaultValue = "0") Integer offset) {
        return blogService.queryBlogOfFollow(currentUserId, max, offset);
    }
}
