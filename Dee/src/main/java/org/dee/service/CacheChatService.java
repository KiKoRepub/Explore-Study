package org.dee.service;

import org.dee.enums.PersistenceType;
import org.springframework.ai.chat.messages.Message;

import java.util.List;

/**
 * Redis 聊天记录管理服务
 */
public interface CacheChatService {

    /**
     * 保存聊天消息到 Redis
     * @param conversationId 对话ID
     * @param userMessage 用户消息
     * @param botResponse 机器人回复
     * @param expireSeconds 过期时间（秒）
     */
    boolean cacheChatMessage(String conversationKey, String userMessage, String botResponse,long expireSeconds);

    /**
     * 获取对话的所有消息
     * @param conversationId 对话ID
     * @param clazz 消息类型
     * @return 消息列表
     */
    <T> List<T> getCachedChatMessages(String conversationKey,Class<T> clazz);

    /**
     * 批量持久化对话记录到数据库
     * @param conversationId 对话ID
     * @param userId 用户ID
     * @param persistenceType 持久化类型（自动/手动）
     */
    void persistChatMessages(String conversationId, String userId, PersistenceType persistenceType);
}
