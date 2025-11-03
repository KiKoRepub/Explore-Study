package org.dee.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import lombok.extern.slf4j.Slf4j;
import org.dee.service.impl.RedisCacheChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import javax.annotation.PostConstruct;

/**
 * Redis 配置类
 * 
 * 功能：
 * 1. 配置 RedisTemplate（支持多线程安全）
 * 2. 配置 Redis 连接工厂
 * 3. 启用键空间通知（用于自动持久化）
 * 4. 配置消息监听容器
 * 
 * 注意：仅在使用 RedisCacheChatService 时启用
 */
@Slf4j
@Configuration
@ConditionalOnBean(RedisCacheChatService.class)
public class RedisConfig {

    /**
     * Redis 连接配置
     */
    private static final String REDIS_HOST = "127.0.0.1";
    private static final int REDIS_PORT = 6379;
    private static final String REDIS_PASSWORD = "redis";
    private static final int REDIS_DATABASE = 0;



    /**
     * 配置 Redis 连接工厂
     * 使用 Lettuce 客户端（支持连接池，线程安全）
     */
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(REDIS_HOST);
        config.setPort(REDIS_PORT);
        config.setPassword(REDIS_PASSWORD);
        config.setDatabase(REDIS_DATABASE);

        LettuceConnectionFactory factory = new LettuceConnectionFactory(config);
        factory.afterPropertiesSet();

        log.info("✓ Redis 连接工厂已配置: {}:{}", REDIS_HOST, REDIS_PORT);

        enableKeyspaceNotifications(factory);
        return factory;
    }

    /**
     * 配置 RedisTemplate
     * 用于操作 Redis，支持多线程安全
     * 
     * 序列化配置：
     * - Key: String 序列化
     * - Value: JSON 序列化
     * - HashKey: String 序列化
     * - HashValue: JSON 序列化
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);


        ObjectMapper objectMapper = new ObjectMapper();
        // 指定要序列化的域，field, get 和 set, 以及修饰符范围，ANY 是都有包括 private 和 public
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        // 指定序列化输入的类型，类必须是非 final 修饰的
        objectMapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL
        );
        // 使用 Jackson2JsonRedisSerializer 来序列化和反序列化 redis 的 value 值
        Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer =
                new Jackson2JsonRedisSerializer<>(objectMapper,Object.class);


        // String 序列化
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();

        // key 采用 String 的序列化方式
        template.setKeySerializer(stringRedisSerializer);
        // hash 的 key 也采用 String 的序列化方式
        template.setHashKeySerializer(stringRedisSerializer);
        // value 序列化方式采用 jackson
        template.setValueSerializer(jackson2JsonRedisSerializer);
        // hash 的 value 序列化方式采用 jackson
        template.setHashValueSerializer(jackson2JsonRedisSerializer);

        template.afterPropertiesSet();
        
        log.info("✓ RedisTemplate 已配置（支持多线程安全）");
        return template;
    }

    /**
     * 配置 StringRedisTemplate
     * 用于简单的字符串操作
     */
    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
        StringRedisTemplate template = new StringRedisTemplate();
        template.setConnectionFactory(connectionFactory);
        template.afterPropertiesSet();
        
        log.info("✓ StringRedisTemplate 已配置");
        return template;
    }

    /**
     * 配置 Redis 消息监听容器
     * 用于监听键过期事件
     */
    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(RedisConnectionFactory connectionFactory) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        
        log.info("✓ Redis 消息监听容器已配置");
        return container;
    }

    /**
     * 启用 Redis 键空间通知
     * 用于监听键过期事件，实现自动持久化
     * 
     * 注意：使用注入的 redisConnectionFactory，避免循环依赖
     */
    public void enableKeyspaceNotifications(RedisConnectionFactory factory) {
        try {

            factory.getConnection().setConfig("notify-keyspace-events", "Ex");
            
            log.info("✓ Redis 键空间通知已启用: notify-keyspace-events=Ex");
            log.info("✓ 自动持久化功能已激活");

        } catch (Exception e) {
            log.error("❌ 启用 Redis 键空间通知失败", e);
            log.warn("⚠️ 自动持久化功能可能无法正常工作");
        }
    }
}
