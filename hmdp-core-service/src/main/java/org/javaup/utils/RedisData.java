package org.javaup.utils;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @description: redis数据
 * @maintainer: lrb
 **/
@Data
public class RedisData {
    private LocalDateTime expireTime;
    private Object data;
}
