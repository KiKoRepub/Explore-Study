package org.dee.listener;

import lombok.extern.slf4j.Slf4j;
import org.dee.enums.PersistenceType;
import org.dee.service.CacheChatService;
import org.dee.service.impl.RedisCacheChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.listener.KeyExpirationEventMessageListener;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Redis 键过期事件监听器
 * 监听聊天记录的过期事件，自动触发批量持久化
 * 
 * 注意：仅在使用 RedisCacheChatService 时启用
 * 如果使用 DefaultCacheChatServiceImpl（内存缓存），此监听器不会被创建
 * 
 * 工作原理：
 * 1. 当聊天记录的过期标记键（chat:expire:{userId}:{conversationId}）过期时
 * 2. Redis 发送键过期事件
 * 3. 监听器捕获事件并提取 userId 和 conversationId
 * 4. 异步执行持久化操作
 */
@Slf4j
@Component
@ConditionalOnBean(RedisCacheChatService.class)
public class RedisKeyExpirationListener extends KeyExpirationEventMessageListener {

    /**
     * 聊天记录过期键前缀
     * 格式: chat:expire:{userId}:{conversationId}
     */
    private static final String CHAT_EXPIRE_KEY_PREFIX = "chat:expire:";

    @Autowired
    private CacheChatService cacheChatService;

    public RedisKeyExpirationListener(RedisMessageListenerContainer listenerContainer) {
        super(listenerContainer);
        log.info("✓ Redis 键过期监听器已启动");
    }

    /**
     * 处理键过期事件
     * 当 Redis 键过期时，此方法会被自动调用
     * 
     * @param message 过期的键名
     * @param pattern 匹配模式
     */
    @Override
    @Async // 异步执行，避免阻塞 Redis 事件处理
    public void onMessage(Message message, byte[] pattern) {
        String expiredKey = message.toString();
        log.info("🔔 检测到 Redis 键过期: {}", expiredKey);

        // 只处理聊天过期键
        if (expiredKey.startsWith(CHAT_EXPIRE_KEY_PREFIX)) {
            String conversationKey = expiredKey.substring(CHAT_EXPIRE_KEY_PREFIX.length());
            log.info("⚡ 触发自动持久化: conversationKey={}", conversationKey);

            try {
                // 从 conversationKey 中提取 conversationId 和 userId
                // conversationKey 格式: userId:conversationId
                String[] parts = conversationKey.split(":");
                if (parts.length >= 2) {
                    String userId = parts[0];
                    String conversationId = parts[1];
                    
                    // 执行自动持久化操作（传入 AUTO 类型）
                    cacheChatService.persistChatMessages(conversationId, userId, PersistenceType.AUTO);
                    log.info("✓ 自动持久化成功: conversationId={}, userId={}", conversationId, userId);
                } else {
                    log.warn("⚠️ conversationKey 格式错误: {}", conversationKey);
                }
            } catch (Exception e) {
                log.error("❌ 自动持久化失败: conversationKey={}", conversationKey, e);
                // 可以在这里添加重试逻辑或告警
            }
        }
    }
}
