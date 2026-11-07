package com.howcode.aichat.infrastructure.cache;

import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/** Redis 操作门面。 */
@Component
public class RedisClient {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    public <T> void set(String key, T value) {
        redisTemplate.opsForValue().set(key, value);
    }

    public <T> void set(String key, T value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public <T> T get(String key, Class<T> clazz) {
        Object value = get(key);
        return null == value ? null : clazz.cast(value);
    }

    public boolean delete(String key) {
        return Boolean.TRUE.equals(redisTemplate.delete(key));
    }

    public boolean expire(String key, long timeout, TimeUnit unit) {
        return Boolean.TRUE.equals(redisTemplate.expire(key, timeout, unit));
    }

    public boolean sAdd(String key, Object value) {
        Long count = redisTemplate.opsForSet().add(key, value);
        return null != count && count > 0;
    }

    public boolean sIsMember(String key, Object value) {
        return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(key, value));
    }
}
