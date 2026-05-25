package org.javaup.user.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import org.javaup.dto.Result;
import org.javaup.dto.UserDTO;
import org.javaup.user.constant.UserRedisConstants;
import org.javaup.user.entity.User;
import org.javaup.user.entity.UserInfo;
import org.javaup.user.mapper.UserInfoMapper;
import org.javaup.user.mapper.UserMapper;
import org.javaup.user.service.UserService;
import org.springframework.data.redis.connection.BitFieldSubCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl implements UserService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private UserInfoMapper userInfoMapper;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Result<UserDTO> queryUserById(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.ok();
        }
        return Result.ok(BeanUtil.copyProperties(user, UserDTO.class));
    }

    @Override
    public Result<UserInfo> queryUserInfoByUserId(Long userId) {
        UserInfo info = userInfoMapper.selectOne(
                new LambdaQueryWrapper<UserInfo>().eq(UserInfo::getUserId, userId));
        if (info == null) {
            return Result.ok();
        }
        info.setCreateTime(null);
        info.setUpdateTime(null);
        return Result.ok(info);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> updateLevel(Long currentUserId, Integer newLevel) {
        if (Objects.isNull(currentUserId)) {
            return Result.fail("未登录");
        }
        if (Objects.isNull(newLevel) || newLevel <= 0) {
            return Result.fail("参数非法：newLevel");
        }
        UserInfo userInfo = userInfoMapper.selectOne(
                new LambdaQueryWrapper<UserInfo>().eq(UserInfo::getUserId, currentUserId));
        if (userInfo == null) {
            return Result.fail("用户信息不存在");
        }
        if (Objects.equals(userInfo.getLevel(), newLevel)) {
            return Result.ok();
        }
        UserInfo update = new UserInfo();
        update.setId(userInfo.getId());
        update.setLevel(newLevel);
        int updated = userInfoMapper.updateById(update);
        if (updated <= 0) {
            return Result.fail("更新等级失败");
        }
        stringRedisTemplate.delete(UserRedisConstants.USER_INFO_KEY_PREFIX + currentUserId);
        return Result.ok();
    }

    @Override
    public Result<Void> sign(Long currentUserId) {
        if (Objects.isNull(currentUserId)) {
            return Result.fail("未登录");
        }
        LocalDateTime now = LocalDateTime.now();
        String keySuffix = now.format(DateTimeFormatter.ofPattern(":yyyyMM"));
        String key = UserRedisConstants.USER_SIGN_KEY + currentUserId + keySuffix;
        int dayOfMonth = now.getDayOfMonth();
        stringRedisTemplate.opsForValue().setBit(key, dayOfMonth - 1, true);
        stringRedisTemplate.expire(key, 400, TimeUnit.DAYS);
        return Result.ok();
    }

    @Override
    public Result<Integer> signCount(Long currentUserId) {
        if (Objects.isNull(currentUserId)) {
            return Result.fail("未登录");
        }
        LocalDateTime now = LocalDateTime.now();
        String keySuffix = now.format(DateTimeFormatter.ofPattern(":yyyyMM"));
        String key = UserRedisConstants.USER_SIGN_KEY + currentUserId + keySuffix;
        int dayOfMonth = now.getDayOfMonth();
        List<Long> result = stringRedisTemplate.opsForValue().bitField(
                key,
                BitFieldSubCommands.create()
                        .get(BitFieldSubCommands.BitFieldType.unsigned(dayOfMonth)).valueAt(0)
        );
        if (result == null || result.isEmpty() || result.get(0) == null || result.get(0) == 0) {
            return Result.ok(0);
        }
        Long num = result.get(0);
        int count = 0;
        while ((num & 1) != 0) {
            count++;
            num >>>= 1;
        }
        return Result.ok(count);
    }
}
