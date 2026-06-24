package com.irctc.common.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(
            RedisConnectionFactory connectionFactory) {

        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        // connectionFactory comes from application.properties
        // Spring auto-creates it from spring.data.redis.host and port

        // Key serializer — how Redis stores the KEY
        // StringRedisSerializer means keys are stored as plain strings
        // "search:NDLS:BCT" stored exactly as that string
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());

        // Value serializer — how Redis stores the VALUE
        // GenericJackson2JsonRedisSerializer converts Java objects to JSON
        // So a List<TrainSearchResult> becomes a JSON array in Redis
        ObjectMapper objectMapper = new ObjectMapper();

        // JavaTimeModule handles LocalDate, LocalTime, LocalDateTime
        // Without this, Redis can't serialize/deserialize our date fields
        objectMapper.registerModule(new JavaTimeModule());

        // activateDefaultTyping tells Jackson to include the Java class name
        // in the JSON so it knows how to deserialize back to the right type
        // Without this, Redis stores JSON but can't reconstruct the object
        objectMapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );

        GenericJackson2JsonRedisSerializer serializer =
                new GenericJackson2JsonRedisSerializer(objectMapper);

        template.setValueSerializer(serializer);
        template.setHashValueSerializer(serializer);

        template.afterPropertiesSet();
        return template;
    }
}