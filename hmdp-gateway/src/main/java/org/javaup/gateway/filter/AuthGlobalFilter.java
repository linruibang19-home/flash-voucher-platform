package org.javaup.gateway.filter;

import org.javaup.gateway.config.GatewayAuthProperties;
import org.javaup.gateway.constant.GatewayHeaders;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    private static final String USER_ID_FIELD = "id";

    private final GatewayAuthProperties authProperties;
    private final ReactiveStringRedisTemplate redisTemplate;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public AuthGlobalFilter(GatewayAuthProperties authProperties,
                            ReactiveStringRedisTemplate redisTemplate) {
        this.authProperties = authProperties;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        if (!authProperties.isEnabled() || isWhitelisted(exchange)) {
            return chain.filter(exchange);
        }

        String token = exchange.getRequest().getHeaders().getFirst(GatewayHeaders.AUTHORIZATION);
        if (!StringUtils.hasText(token)) {
            return reject(exchange);
        }

        String redisKey = authProperties.getLoginUserKeyPrefix() + token;
        return redisTemplate.<String, String>opsForHash()
                .get(redisKey, USER_ID_FIELD)
                .flatMap(userId -> {
                    if (!StringUtils.hasText(userId)) {
                        return reject(exchange);
                    }
                    ServerHttpRequest request = exchange.getRequest()
                            .mutate()
                            .header(GatewayHeaders.USER_ID, userId)
                            .build();
                    ServerWebExchange mutatedExchange = exchange.mutate().request(request).build();
                    return redisTemplate.expire(redisKey, authProperties.getTokenTtl())
                            .then(chain.filter(mutatedExchange));
                })
                .switchIfEmpty(reject(exchange));
    }

    private boolean isWhitelisted(ServerWebExchange exchange) {
        String path = exchange.getRequest().getURI().getPath();
        return authProperties.getWhitelist().stream()
                .anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

    private Mono<Void> reject(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 5;
    }
}
