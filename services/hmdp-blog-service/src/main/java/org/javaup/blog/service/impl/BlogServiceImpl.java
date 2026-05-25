package org.javaup.blog.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.javaup.blog.constant.BlogConstants;
import org.javaup.blog.entity.Blog;
import org.javaup.blog.entity.Follow;
import org.javaup.blog.mapper.BlogMapper;
import org.javaup.blog.service.BlogService;
import org.javaup.blog.service.FollowService;
import org.javaup.blog.service.UserLookupService;
import org.javaup.dto.Result;
import org.javaup.dto.ScrollResult;
import org.javaup.dto.UserDTO;
import org.javaup.toolkit.SnowflakeIdGenerator;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Service
public class BlogServiceImpl extends ServiceImpl<BlogMapper, Blog> implements BlogService {

    @Resource
    private UserLookupService userLookupService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private FollowService followService;

    @Resource
    private SnowflakeIdGenerator snowflakeIdGenerator;

    @Override
    public Result<?> queryHotBlog(Long currentUserId, Integer current) {
        Page<Blog> page = query()
                .orderByDesc("liked")
                .page(new Page<>(current, BlogConstants.MAX_PAGE_SIZE));
        List<Blog> records = page.getRecords();
        records.forEach(blog -> {
            queryBlogUser(blog);
            isBlogLiked(currentUserId, blog);
        });
        return Result.ok(records);
    }

    @Override
    public Result<?> queryBlogById(Long currentUserId, Long id) {
        Blog blog = getById(id);
        if (blog == null) {
            return Result.fail("Blog does not exist");
        }
        queryBlogUser(blog);
        isBlogLiked(currentUserId, blog);
        return Result.ok(blog);
    }

    @Override
    public Result<?> likeBlog(Long currentUserId, Long id) {
        if (currentUserId == null) {
            return Result.fail("User is not logged in");
        }
        String key = BlogConstants.BLOG_LIKED_KEY + id;
        Double score = stringRedisTemplate.opsForZSet().score(key, currentUserId.toString());
        if (score == null) {
            boolean isSuccess = update().setSql("liked = liked + 1").eq("id", id).update();
            if (isSuccess) {
                stringRedisTemplate.opsForZSet().add(key, currentUserId.toString(), System.currentTimeMillis());
            }
        } else {
            boolean isSuccess = update().setSql("liked = liked - 1").eq("id", id).update();
            if (isSuccess) {
                stringRedisTemplate.opsForZSet().remove(key, currentUserId.toString());
            }
        }
        return Result.ok();
    }

    @Override
    public Result<?> queryBlogLikes(Long id) {
        String key = BlogConstants.BLOG_LIKED_KEY + id;
        Set<String> top5 = stringRedisTemplate.opsForZSet().range(key, 0, 4);
        if (top5 == null || top5.isEmpty()) {
            return Result.ok(Collections.emptyList());
        }
        List<Long> ids = top5.stream().map(Long::valueOf).toList();
        // listUserDTOsByIds 内部按 ids 顺序返回，保留点赞时间排序
        List<UserDTO> userDTOS = userLookupService.listUserDTOsByIds(ids);
        return Result.ok(userDTOS);
    }

    @Override
    public Result<?> saveBlog(Long currentUserId, Blog blog) {
        if (currentUserId == null) {
            return Result.fail("User is not logged in");
        }
        blog.setId(snowflakeIdGenerator.nextId());
        blog.setUserId(currentUserId);
        boolean isSuccess = save(blog);
        if (!isSuccess) {
            return Result.fail("Create blog failed");
        }

        List<Follow> follows = followService.query().eq("follow_user_id", currentUserId).list();
        for (Follow follow : follows) {
            String key = BlogConstants.FEED_KEY + follow.getUserId();
            stringRedisTemplate.opsForZSet().add(key, blog.getId().toString(), System.currentTimeMillis());
        }
        return Result.ok(blog.getId());
    }

    @Override
    public Result<?> queryBlogOfFollow(Long currentUserId, Long max, Integer offset) {
        if (currentUserId == null) {
            return Result.fail("User is not logged in");
        }
        String key = BlogConstants.FEED_KEY + currentUserId;
        Set<ZSetOperations.TypedTuple<String>> typedTuples = stringRedisTemplate.opsForZSet()
                .reverseRangeByScoreWithScores(key, 0, max, offset, 2);
        if (typedTuples == null || typedTuples.isEmpty()) {
            return Result.ok();
        }

        List<Long> ids = new ArrayList<>(typedTuples.size());
        long minTime = 0;
        int os = 1;
        for (ZSetOperations.TypedTuple<String> tuple : typedTuples) {
            ids.add(Long.valueOf(tuple.getValue()));
            long time = tuple.getScore().longValue();
            if (time == minTime) {
                os++;
            } else {
                minTime = time;
                os = 1;
            }
        }

        String idStr = StrUtil.join(",", ids);
        List<Blog> blogs = query().in("id", ids).last("ORDER BY FIELD(id," + idStr + ")").list();
        for (Blog blog : blogs) {
            queryBlogUser(blog);
            isBlogLiked(currentUserId, blog);
        }

        ScrollResult r = new ScrollResult();
        r.setList(blogs);
        r.setOffset(os);
        r.setMinTime(minTime);
        return Result.ok(r);
    }

    private void isBlogLiked(Long currentUserId, Blog blog) {
        if (currentUserId == null) {
            return;
        }
        String key = BlogConstants.BLOG_LIKED_KEY + blog.getId();
        Double score = stringRedisTemplate.opsForZSet().score(key, currentUserId.toString());
        blog.setIsLike(score != null);
    }

    private void queryBlogUser(Blog blog) {
        UserDTO user = userLookupService.getUserById(blog.getUserId());
        if (user != null) {
            blog.setName(user.getNickName());
            blog.setIcon(user.getIcon());
        }
    }
}
