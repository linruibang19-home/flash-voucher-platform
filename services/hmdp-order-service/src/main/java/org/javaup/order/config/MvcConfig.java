package org.javaup.order.config;

import cn.hutool.core.util.StrUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.javaup.dto.UserDTO;
import org.javaup.order.utils.UserHolder;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @description: MVC 拦截器配置：从 Gateway 注入的 X-User-Id 请求头填充 UserHolder
 * @maintainer: lrb
 **/
@Configuration
public class MvcConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new UserContextInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns("/internal/**", "/actuator/**");
    }

    /**
     * 轻量拦截器：仅读取 X-User-Id 请求头并写入 ThreadLocal
     */
    private static class UserContextInterceptor implements HandlerInterceptor {

        private static final String HEADER_USER_ID = "X-User-Id";

        @Override
        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
                throws Exception {
            String userId = request.getHeader(HEADER_USER_ID);
            if (StrUtil.isBlank(userId)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return false;
            }
            try {
                UserDTO userDTO = new UserDTO();
                userDTO.setId(Long.valueOf(userId));
                UserHolder.saveUser(userDTO);
            } catch (NumberFormatException e) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return false;
            }
            return true;
        }

        @Override
        public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                    Object handler, Exception ex) {
            UserHolder.removeUser();
        }
    }
}
