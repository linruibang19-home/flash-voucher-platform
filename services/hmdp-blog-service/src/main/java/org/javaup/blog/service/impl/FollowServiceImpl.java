package org.javaup.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.javaup.blog.constant.BlogConstants;
import org.javaup.blog.entity.Follow;
import org.javaup.blog.mapper.FollowMapper;
import org.javaup.blog.service.FollowService;
import org.javaup.blog.service.UserLookupService;
import org.javaup.dto.Result;
import org.javaup.dto.UserDTO;
import org.javaup.toolkit.SnowflakeIdGenerator;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Set;

@Service
public class FollowServiceImpl extends ServiceImpl<FollowMapper, Follow> implements FollowService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private UserLookupService userLookupService;

    @Resource
    private SnowflakeIdGenerator snowflakeIdGenerator;

    @Override
    public Result<?> follow(Long currentUserId, Long followUserId, Boolean isFollow) {
        if (currentUserId == null) {
            return Result.fail("User is not logged in");
        }
        String key = BlogConstants.FOLLOW_KEY + currentUserId;
        if (isFollow) {
            Follow follow = new Follow();
            follow.setId(snowflakeIdGenerator.nextId());
            follow.setUserId(currentUserId);
            follow.setFollowUserId(followUserId);
            boolean isSuccess = save(follow);
            if (isSuccess) {
                stringRedisTemplate.opsForSet().add(key, followUserId.toString());
            }
        } else {
            boolean isSuccess = remove(new QueryWrapper<Follow>()
                    .eq("user_id", currentUserId)
                    .eq("follow_user_id", followUserId));
            if (isSuccess) {
                stringRedisTemplate.opsForSet().remove(key, followUserId.toString());
            }
        }
        return Result.ok();
    }

    @Override
    public Result<?> isFollow(Long currentUserId, Long followUserId) {
        if (currentUserId == null) {
            return Result.fail("User is not logged in");
        }
        Long count = query()
                .eq("user_id", currentUserId)
                .eq("follow_user_id", followUserId)
                .count();
        return Result.ok(count > 0);
    }

    @Override
    public Result<?> followCommons(Long currentUserId, Long targetUserId) {
        if (currentUserId == null) {
            return Result.fail("User is not logged in");
        }
        String key = BlogConstants.FOLLOW_KEY + currentUserId;
        String targetKey = BlogConstants.FOLLOW_KEY + targetUserId;
        Set<String> intersect = stringRedisTemplate.opsForSet().intersect(key, targetKey);
        if (intersect == null || intersect.isEmpty()) {
            return Result.ok(Collections.emptyList());
        }
        List<Long> ids = intersect.stream().map(Long::valueOf).toList();
        List<UserDTO> users = userLookupService.listUserDTOsByIds(ids);
        return Result.ok(users);
    }
}
