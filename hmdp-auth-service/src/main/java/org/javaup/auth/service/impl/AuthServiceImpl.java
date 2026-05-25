package org.javaup.auth.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.javaup.auth.constant.AuthRedisConstants;
import org.javaup.auth.entity.AuthUser;
import org.javaup.auth.entity.AuthUserInfo;
import org.javaup.auth.entity.AuthUserPhone;
import org.javaup.auth.mapper.AuthUserInfoMapper;
import org.javaup.auth.mapper.AuthUserMapper;
import org.javaup.auth.mapper.AuthUserPhoneMapper;
import org.javaup.auth.service.AuthService;
import org.javaup.auth.util.AuthRegexUtils;
import org.javaup.dto.LoginFormDTO;
import org.javaup.dto.Result;
import org.javaup.dto.UserDTO;
import org.javaup.toolkit.SnowflakeIdGenerator;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class AuthServiceImpl implements AuthService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private SnowflakeIdGenerator snowflakeIdGenerator;

    @Resource
    private AuthUserMapper userMapper;

    @Resource
    private AuthUserInfoMapper userInfoMapper;

    @Resource
    private AuthUserPhoneMapper userPhoneMapper;

    @Override
    public Result<String> sendCode(String phone, HttpSession session) {
        if (AuthRegexUtils.isPhoneInvalid(phone)) {
            return Result.fail("手机号格式错误！");
        }
        String code = RandomUtil.randomNumbers(6);
        stringRedisTemplate.opsForValue().set(
                AuthRedisConstants.LOGIN_CODE_KEY + phone,
                code,
                AuthRedisConstants.LOGIN_CODE_TTL_MINUTES,
                TimeUnit.MINUTES);
        log.info("send login code success, phone={}, code={}", phone, code);
        return Result.ok(code);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> login(LoginFormDTO loginForm, HttpSession session) {
        String phone = loginForm.getPhone();
        if (AuthRegexUtils.isPhoneInvalid(phone)) {
            return Result.fail("手机号格式错误！");
        }
        String cacheCode = stringRedisTemplate.opsForValue().get(AuthRedisConstants.LOGIN_CODE_KEY + phone);
        if (cacheCode == null || !cacheCode.equals(loginForm.getCode())) {
            return Result.fail("验证码错误");
        }

        AuthUserPhone userPhone = userPhoneMapper.selectOne(
                new LambdaQueryWrapper<AuthUserPhone>().eq(AuthUserPhone::getPhone, phone));
        AuthUser user = userPhone == null ? createUserWithPhone(phone) : findUserByPhone(userPhone.getPhone());
        if (user == null) {
            return Result.fail("用户不存在");
        }

        String token = UUID.randomUUID().toString(true);
        UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);
        Map<String, Object> userMap = BeanUtil.beanToMap(userDTO, new HashMap<>(),
                CopyOptions.create()
                        .setIgnoreNullValue(true)
                        .setFieldValueEditor((fieldName, fieldValue) -> fieldValue.toString()));

        String tokenKey = AuthRedisConstants.LOGIN_USER_KEY + token;
        stringRedisTemplate.opsForHash().putAll(tokenKey, userMap);
        stringRedisTemplate.expire(
                tokenKey,
                TimeUnit.SECONDS.convert(AuthRedisConstants.LOGIN_USER_TTL_MINUTES, TimeUnit.MINUTES),
                TimeUnit.SECONDS);
        return Result.ok(token);
    }

    private AuthUser findUserByPhone(String phone) {
        return userMapper.selectOne(new LambdaQueryWrapper<AuthUser>().eq(AuthUser::getPhone, phone));
    }

    private AuthUser createUserWithPhone(String phone) {
        AuthUser user = new AuthUser();
        user.setId(snowflakeIdGenerator.nextId());
        user.setPhone(phone);
        user.setNickName(AuthRedisConstants.USER_NICK_NAME_PREFIX + RandomUtil.randomString(10));
        userMapper.insert(user);

        AuthUserInfo userInfo = new AuthUserInfo();
        userInfo.setId(snowflakeIdGenerator.nextId());
        userInfo.setUserId(user.getId());
        userInfo.setLevel(1);
        userInfoMapper.insert(userInfo);

        AuthUserPhone userPhone = new AuthUserPhone();
        userPhone.setId(snowflakeIdGenerator.nextId());
        userPhone.setUserId(user.getId());
        userPhone.setPhone(phone);
        userPhoneMapper.insert(userPhone);
        return user;
    }
}
