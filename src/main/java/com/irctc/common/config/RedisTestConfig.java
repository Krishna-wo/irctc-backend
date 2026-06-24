package com.irctc.common.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class RedisTestConfig {

    private final RedisTemplate<String, Object> redisTemplate;

    @Bean
    public ApplicationRunner testRedisConnection() {
        return args -> {
            try {

                redisTemplate.opsForValue().set("test:connection", "Redis is working!");

                // Read it back
                Object value = redisTemplate.opsForValue().get("test:connection");

                log.info("=== REDIS TEST: {} ===", value);
                // You should see: REDIS TEST: Redis is working!

                // Clean up — delete the test key
                redisTemplate.delete("test:connection");

            } catch (Exception e) {
                log.error("=== REDIS CONNECTION FAILED: {} ===", e.getMessage());
            }
        };
    }
}