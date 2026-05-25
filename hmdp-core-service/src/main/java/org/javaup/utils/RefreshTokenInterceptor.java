package org.javaup.utils;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.javaup.dto.UserDTO;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static org.javaup.utils.RedisConstants.LOGIN_USER_KEY;
import static org.javaup.utils.RedisConstants.LOGIN_USER_TTL;

/**
 * @description: token and gateway user-context interceptor
 * @maintainer: lrb
 **/
public class RefreshTokenInterceptor implements HandlerInterceptor {

    private final StringRedisTemplate stringRedisTemplate;

    public RefreshTokenInterceptor(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        UserDTO gatewayUser = resolveGatewayUser(request);
        if (Objects.nonNull(gatewayUser)) {
            UserHolder.saveUser(gatewayUser);
            fillUserFromTokenIfPresent(request);
            return true;
        }
        String token = request.getHeader(GatewayHeaders.AUTHORIZATION);
        if (StrUtil.isBlank(token)) {
            return true;
        }
        loadUserFromToken(token);
        return true;
    }

    private UserDTO resolveGatewayUser(HttpServletRequest request) {
        String userId = request.getHeader(GatewayHeaders.USER_ID);
        if (StrUtil.isBlank(userId)) {
            return null;
        }
        try {
            UserDTO userDTO = new UserDTO();
            userDTO.setId(Long.valueOf(userId));
            return userDTO;
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private void fillUserFromTokenIfPresent(HttpServletRequest request) {
        String token = request.getHeader(GatewayHeaders.AUTHORIZATION);
        if (StrUtil.isBlank(token)) {
            return;
        }
        loadUserFromToken(token);
    }

    private void loadUserFromToken(String token) {
        String key = LOGIN_USER_KEY + token;
        Map<Object, Object> userMap = stringRedisTemplate.opsForHash().entries(key);
        if (userMap.isEmpty()) {
            return;
        }
        UserDTO userDTO = BeanUtil.fillBeanWithMap(userMap, new UserDTO(), false);
        UserHolder.saveUser(userDTO);
        stringRedisTemplate.expire(
                key,
                TimeUnit.SECONDS.convert(LOGIN_USER_TTL, TimeUnit.MINUTES),
                TimeUnit.SECONDS
        );
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserHolder.removeUser();
    }
}
