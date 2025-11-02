package org.dee.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.A;
import org.dee.dto.ChatMessageDTO;
import org.dee.dto.RedisChatMessageDTO;
import org.dee.service.CacheChatService;
import org.dee.service.ChatRecordService;
import org.dee.service.ChatSummaryService;
import org.dee.utlis.RedisUtils;
import org.springframework.ai.chat.messages.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
public class RedisCacheChatService implements CacheChatService {
    @Autowired
    ChatRecordService chatRecordService;

    @Autowired
    ChatSummaryService chatSummaryService;
    @Override
    public boolean cacheChatMessage(String conversationId, String userMessage, String botResponse, long expireSeconds) {
        RedisChatMessageDTO  redisMessageDTO = new RedisChatMessageDTO(userMessage, botResponse);

        return RedisUtils.pushCacheRecordList(conversationId,
                Collections.singletonList(redisMessageDTO));


    }

    @Override
    public <T> List<T> getCachedChatMessages(String conversationId, Class<T> clazz) {
        try {
            if (clazz == RedisChatMessageDTO.class) {
                return (List<T>) RedisUtils.getCacheAIRecordList(conversationId);
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
    public void persistChatMessages(String conversationId) {
        log.info("开始持久化对话记录: conversationId={}", conversationId);

        // 1. 获取 内存 中的所有消息
        List<RedisChatMessageDTO> messages = getChatMessages(conversationId);

        if (messages.isEmpty()) {
            log.warn("没有找到需要持久化的消息: conversationId={}", conversationId);
            return;
        }


        // 将消息按用户和助手配对
        List<ChatMessageDTO> chatMessageDTOList = convertMessage(messages);

        // 批量保存聊天记录

        chatRecordService.batchSaveChatRecords(conversationId,chatMessageDTOList);


        log.info("批量保存聊天记录完成: conversationId={}, 总数={}",
                conversationId, chatMessageDTOList.size());

        // 3. 生成对话摘要
        String summary = chatSummaryService.generateSummary(chatMessageDTOList);
        String title = generateTitle(messages);

        // 4. 保存概要到数据库
        boolean summarySuccess = chatRecordService.saveChatRecordZip(conversationId, title, summary);
        log.info("保存对话概要: conversationId={}, 成功={}", conversationId, summarySuccess);

        // 5. 清理 内存 中的聊天记录
        RedisUtils.removeAIRecordCache(conversationId);
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
