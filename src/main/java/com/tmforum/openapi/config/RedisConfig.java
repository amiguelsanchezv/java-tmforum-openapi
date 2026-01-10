package com.tmforum.openapi.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.Objects;

/**
 * Redis Configuration for caching
 * Activated only if redis.enabled=true in application.properties
 * Note: RedisConnectionFactory is auto-configured by Spring Boot
 * using the spring.data.redis.* properties from the ConfigMap
 */
@Configuration
@EnableCaching
@ConditionalOnProperty(name = "redis.enabled", havingValue = "true", matchIfMissing = false)
public class RedisConfig {
    
    // Removed redisConnectionFactory() - Spring Boot auto-configures it
    // using spring.data.redis.host and spring.data.redis.port from the ConfigMap
    
    /**
     * ObjectMapper configured for Redis with support for LocalDateTime
     */
    @Bean
    public ObjectMapper redisObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }
    
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory, ObjectMapper redisObjectMapper) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        
        // Use ObjectMapper configured for LocalDateTime
        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(Objects.requireNonNull(redisObjectMapper));
        template.setValueSerializer(serializer);
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(serializer);
        template.afterPropertiesSet();
        return template;
    }
    
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory, ObjectMapper redisObjectMapper) {
        // Use ObjectMapper configured for LocalDateTime
        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(Objects.requireNonNull(redisObjectMapper));
        
        Duration defaultTtl = Objects.requireNonNull(Duration.ofMinutes(10));
        Duration customersTtl = Objects.requireNonNull(Duration.ofMinutes(5));
        Duration customersListTtl = Objects.requireNonNull(Duration.ofMinutes(2));
        
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(defaultTtl) // Default TTL: 10 minutes
            .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
            .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer))
            .disableCachingNullValues();
        
        RedisConnectionFactory factory = Objects.requireNonNull(connectionFactory);
        return RedisCacheManager.builder(factory)
            .cacheDefaults(config)
            .withCacheConfiguration("customers", 
                RedisCacheConfiguration.defaultCacheConfig().entryTtl(customersTtl))
            .withCacheConfiguration("customersList", 
                RedisCacheConfiguration.defaultCacheConfig().entryTtl(customersListTtl))
            .build();
    }
}

