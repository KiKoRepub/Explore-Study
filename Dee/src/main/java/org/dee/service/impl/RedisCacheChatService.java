package org.dee.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.dee.dto.ChatMessageDTO;
import org.dee.dto.RedisChatMessageDTO;
import org.dee.enums.PersistenceType;
import org.dee.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Redis 缓存聊天服务实现
 * 使用 RedisService 进行线程安全的 Redis 操作
 */
@Slf4j
//@Service
public class RedisCacheChatService implements CacheChatService {
    
    @Autowired
    private ChatContextService chatContextService;

    @Autowired
    private RedisService redisService;
    @Override
    public boolean cacheChatMessage(String conversationId, String userMessage, String botResponse, long expireSeconds) {
        RedisChatMessageDTO redisMessageDTO = new RedisChatMessageDTO(userMessage, botResponse);

        // 1. 保存聊天消息到 Redis（使用线程安全的 RedisService）
        boolean saved = redisService.pushCacheRecordList(conversationId,
                Collections.singletonList(redisMessageDTO),
                expireSeconds);

        if (saved) {
            // 2. 设置过期标记键，用于触发自动持久化
            // 当这个键过期时，会触发 Redis 键过期事件，监听器会自动执行持久化
            redisService.setExpireMarker(conversationId,expireSeconds);
            log.info("✓ 聊天消息已缓存，将在 {} 秒后自动持久化: conversationId={}", expireSeconds, conversationId);
        }

        return saved;
    }

    @Override
    public <T> List<T> getCachedChatMessages(String conversationId, Class<T> clazz) {
        try {
            if (clazz == RedisChatMessageDTO.class) {
                return (List<T>) redisService.getCacheAIRecordList(conversationId);
            } else {
                throw new UnsupportedOperationException("不支持的消息类型: " + clazz.getName());
            }
        } catch (Exception e) {
            log.error("获取缓存失败: conversationId={}", conversationId, e);
            return new ArrayList<>();
        }
    }


    private List<RedisChatMessageDTO> getChatMessages(String conversationId) {
        return getCachedChatMessages(conversationId,RedisChatMessageDTO.class);
    }

    @Override
    public void persistChatMessages(String conversationId, PersistenceType persistenceType) {
        String typeDesc = persistenceType.getDescription();
        log.info("📦 开始持久化对话记录: conversationId={}, 类型={}", conversationId, typeDesc);

        // 1. 获取 Redis 中的所有消息
        List<RedisChatMessageDTO> messages = getChatMessages(conversationId);

        if (messages.isEmpty()) {
            log.warn("⚠️ 没有找到需要持久化的消息: conversationId={}", conversationId);
            return;
        }

        // 2. 将消息转换为 DTO
        List<ChatMessageDTO> chatMessageDTOList = convertMessage(messages);

        // 3. 批量保存聊天记录到数据库
        boolean saveSuccess = chatContextService.batchSaveChatRecords(conversationId, chatMessageDTOList, persistenceType.getCode());
        
        if (saveSuccess) {
            log.info("✓ 批量保存聊天记录完成: conversationId={}, 总数={}, 类型={}", 
                    conversationId, chatMessageDTOList.size(), typeDesc);
        } else {
            log.error("❌ 批量保存聊天记录失败: conversationId={}", conversationId);
        }

        // 4. 生成对话摘要
        String summary = chatContextService.generateSummary(chatMessageDTOList);
        String title = generateTitle(messages);

        // 5. 保存概要到数据库（包含持久化类型）
        boolean summarySuccess = chatContextService.saveChatRecordZip(conversationId, title, summary, persistenceType.getCode());
        log.info("✓ 保存对话概要: conversationId={}, 成功={}, 标题={}, 类型={}", 
                conversationId, summarySuccess, title, typeDesc);

        // 6. 清理 Redis 中的聊天记录
        redisService.removeAIRecordCache(conversationId);
        log.info("✓ 清理 Redis 缓存: conversationId={}", conversationId);

        // 7. 清理过期标记键（如果是手动触发的持久化）
        redisService.removeExpireMarker(conversationId);
        log.info("✓ 清理过期标记: conversationId={}", conversationId);
        
        log.info("🎉 持久化完成: conversationId={}, 类型={}", conversationId, typeDesc);
    }



    private List<ChatMessageDTO> convertMessage(List<RedisChatMessageDTO> messages) {
        return messages.stream()
                .map(msg ->
                    new ChatMessageDTO(msg.getUserMessage(), msg.getBotResponse())
                ).toList();
    }

    private String generateTitle(List<RedisChatMessageDTO> messages) {
        if (messages.isEmpty()) return "无标题对话";

        String firstUserMessage = messages.get(0).getUserMessage();
        if (firstUserMessage.length() <= 10) {
            return firstUserMessage;
        } else {
            return firstUserMessage.substring(0, 10) + "...";
        }
    }
}
