package org.dee.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.dee.dto.ChatMessageDTO;
import org.dee.service.ChatRecordService;
import org.dee.service.ChatSummaryService;
import org.dee.service.CacheChatService;
import org.jetbrains.annotations.NotNull;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 默认的 聊天记录管理服务实现
 *  当不存在 用户自定义的 CacheChatService  时，使用此默认实现
 */
@Slf4j
//@Service
//@ConditionalOnMissingBean(CacheChatService.class)
public class DefaultCacheChatServiceImpl implements CacheChatService {




    private final ChatMemory chatMemory;


    private final ChatRecordService chatRecordService;


    private final ChatSummaryService chatSummaryService;

    public DefaultCacheChatServiceImpl(ChatRecordService recordService, ChatSummaryService summaryService) {

        // 使用基于内存的聊天记录存储
        this.chatMemory = MessageWindowChatMemory.builder()
                .chatMemoryRepository(new InMemoryChatMemoryRepository())
                .maxMessages(10) // 最大消息数
                .build();// 开启内存 信息存储功能

        this.chatRecordService = recordService;
        this.chatSummaryService = summaryService;
    }
    @Override
    public boolean cacheChatMessage(String conversationId, String userMessage, String botResponse, long expireSeconds) {
        try {
            Message userMsg = UserMessage.builder()
                    .text(userMessage)
                    .build();
            Message botMsg = new AssistantMessage(
                    botResponse
            );

            chatMemory.add(conversationId, userMsg);
            chatMemory.add(conversationId, botMsg);

            log.info("保存聊天消息到 内存: conversationId={}, 过期时间={}秒", conversationId, expireSeconds);
            return true;
        }catch (Exception e){
            log.error("保存聊天消息到 内存 失败: conversationId={}, 错误={}", conversationId, e.getMessage());
            return false;
        }
    }


    @Override
    public  <T> List<T> getCachedChatMessages(String conversationId,Class<T> clazz) {
        try {
            List<Message> messages = chatMemory.get(conversationId);

            return (List<T>) convertMessage(messages);
        }catch (Exception e){
            log.error("获取缓存聊天消息失败: conversationId={}, 错误={}", conversationId, e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public void persistChatMessages(String conversationId) {
        log.info("开始持久化对话记录: conversationId={}", conversationId);

        // 1. 获取 内存 中的所有消息
        List<Message> messages = getChatMessages(conversationId);

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

        chatMemory.clear(conversationId);


    }

    /**
     * 将消息列表转换为 ChatMessageDTO 对象列表。
     * 此方法处理消息列表，将用户消息与相应的助手响应配对。
     *
     * @param消息 要转换的消息列表。每条消息都应具有类型（USER 或 ASSISTANT）和文本内容。
     * @return ChatMessageDTO 对象列表，其中每个对象包含用户消息及其配对的助手响应。
     */
    @NotNull
    private static List<ChatMessageDTO> convertMessage(List<Message> messages){
        // 商店助理回复的临时列表
        List<String> botResponseList = new ArrayList<>();

        // 用于存储转换后的 ChatMessageDTO 对象的结果列表
        List<ChatMessageDTO> chatMessageDTOList = new ArrayList<>();

        // 遍历消息
        for (Message message : messages) {
            // 如果消息来自助手，将其文本添加到 botResponseList
            if (message.getMessageType().equals(MessageType.ASSISTANT)) {
                botResponseList.add(message.getText());
            }
            // 如果消息来自用户，请将其与第一个可用的助手响应配对
            if (message.getMessageType().equals(MessageType.USER)) {
                String botResponse = botResponseList.isEmpty() ? "" : botResponseList.remove(0);
                chatMessageDTOList.add(new ChatMessageDTO(message.getText(), botResponse));
            }
        }

        // 返回 ChatMessageDTO 对象列表
        return chatMessageDTOList;
    }

    private List<Message> getChatMessages(String conversationId) {
        return chatMemory.get(conversationId);
    }
    /**
     * 生成对话标题
     */
    private String generateTitle(List<Message> messages) {
        if (messages.isEmpty()) {
            return "空对话";
        }

        String firstMessage = messages.get(0).getText();
        // 取前30个字符作为标题
        return firstMessage.length() > 30 
                ? firstMessage.substring(0, 30) + "..." 
                : firstMessage;
    }
}
