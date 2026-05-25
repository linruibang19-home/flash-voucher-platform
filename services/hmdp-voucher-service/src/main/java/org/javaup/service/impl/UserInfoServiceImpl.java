package org.javaup.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.javaup.core.RedisKeyManage;
import org.javaup.entity.UserInfo;
import org.javaup.enums.BaseCode;
import org.javaup.exception.HmdpFrameException;
import org.javaup.mapper.UserInfoMapper;
import org.javaup.redis.RedisCache;
import org.javaup.redis.RedisKeyBuild;
import org.javaup.service.IUserInfoService;
import org.javaup.servicelock.LockType;
import org.javaup.servicelock.annotion.ServiceLock;
import org.springframework.stereotype.Service;

import java.util.Objects;

import static org.javaup.constant.DistributedLockConstants.UPDATE_USER_INFO_LOCK;

/**
 * @description: 用户信息 接口实现（voucher 域只读，写操作由 hmdp-user-service 负责）
 * @maintainer: lrb
 **/
@Slf4j
@Service
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo> implements IUserInfoService {

    @Resource
    private RedisCache redisCache;

    @Override
    @ServiceLock(lockType= LockType.Read, name = UPDATE_USER_INFO_LOCK, keys = {"#userId"})
    public UserInfo getByUserId(Long userId) {
        UserInfo userInfo = redisCache.get(RedisKeyBuild.createRedisKey(RedisKeyManage.USER_INFO_KEY, userId), UserInfo.class);
        if (Objects.nonNull(userInfo)) {
            return userInfo;
        }
        userInfo = lambdaQuery().eq(UserInfo::getUserId, userId).one();
        if (Objects.isNull(userInfo)) {
            throw new HmdpFrameException(BaseCode.USER_NOT_EXIST);
        }
        redisCache.set(RedisKeyBuild.createRedisKey(RedisKeyManage.USER_INFO_KEY, userId), userInfo);
        return userInfo;
    }
}
