package org.javaup.order.lua;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.javaup.redis.RedisCache;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @description: 秒杀券 Redis 回滚 Lua 脚本执行器（order-service 本地副本）
 * @maintainer: lrb
 **/
@Slf4j
@Component
public class SeckillVoucherRollBackOperate {

    @Resource
    private RedisCache redisCache;

    private DefaultRedisScript<Long> redisScript;

    @PostConstruct
    public void init() {
        try {
            redisScript = new DefaultRedisScript<>();
            redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("lua/seckillVoucherRollBack.lua")));
            redisScript.setResultType(Long.class);
        } catch (Exception e) {
            log.error("redisScript init lua error", e);
        }
    }

    public Integer execute(List<String> keys, String[] args) {
        Object obj = redisCache.getInstance().execute(redisScript, keys, args);
        if (obj instanceof Integer i) {
            return i;
        }
        if (obj instanceof Long l) {
            return l.intValue();
        }
        if (obj instanceof Number n) {
            return n.intValue();
        }
        try {
            return Integer.valueOf(String.valueOf(obj));
        } catch (Exception e) {
            log.warn("Lua回滚脚本返回类型无法转换为Integer: {}", obj);
            return null;
        }
    }
}
