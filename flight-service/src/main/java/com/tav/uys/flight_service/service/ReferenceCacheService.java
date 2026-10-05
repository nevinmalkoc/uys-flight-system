package com.tav.uys.flight_service.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class ReferenceCacheService {

    private final StringRedisTemplate redisTemplate;

    public ReferenceCacheService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void put(String category, String code, String data) {
        String key = buildKey(category, code);
        redisTemplate.opsForValue().set(key, data, 24, TimeUnit.HOURS);
        log.info("[REDIS CACHE PUT] Key: {} -> Value: {}", key, data);
    }

    public boolean exists(String category, String code) {
        String key = buildKey(category, code);
        Boolean hasKey = redisTemplate.hasKey(key);
        log.info("[REDIS CACHE CHECK] Key: {} -> Exists: {}", key, hasKey);
        return Boolean.TRUE.equals(hasKey);
    }

    public void evict(String category, String code) {
        String key = buildKey(category, code);
        redisTemplate.delete(key);
        log.info("[REDIS CACHE EVICT] Key: {} deleted", key);
    }

    private String buildKey(String category, String code) {
        return "ref:" + category.toLowerCase() + ":" + code.toUpperCase();
    }
}
