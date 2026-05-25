package org.javaup.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.javaup.blog.entity.Follow;
import org.javaup.dto.Result;

public interface FollowService extends IService<Follow> {

    Result<?> follow(Long currentUserId, Long followUserId, Boolean isFollow);

    Result<?> isFollow(Long currentUserId, Long followUserId);

    Result<?> followCommons(Long currentUserId, Long targetUserId);
}
