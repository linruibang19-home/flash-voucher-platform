package org.javaup.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.javaup.gateway.constant.GatewayHeaders;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Slf4j
@Component
public class AccessLogGlobalFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        long startNanos = System.nanoTime();
        String method = exchange.getRequest().getMethod().name();
        String path = exchange.getRequest().getURI().getRawPath();
        String requestId = Optional.ofNullable(
                        exchange.getAttribute(RequestIdGlobalFilter.REQUEST_ID_ATTRIBUTE))
                .map(String::valueOf)
                .orElseGet(() -> exchange.getRequest().getHeaders().getFirst(GatewayHeaders.REQUEST_ID));

        return chain.filter(exchange).doFinally(signalType -> {
            long costMillis = (System.nanoTime() - startNanos) / 1_000_000;
            HttpStatusCode status = exchange.getResponse().getStatusCode();
            int statusCode = status == null ? 0 : status.value();
            log.info("gateway access requestId={}, method={}, path={}, status={}, costMs={}",
                    requestId, method, path, statusCode, costMillis);
        });
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 10;
    }
}
