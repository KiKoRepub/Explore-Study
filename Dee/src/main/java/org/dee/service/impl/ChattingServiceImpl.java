package org.dee.service.impl;

import org.dee.dto.ChatMessageDTO;
import org.dee.entity.ChatRecord;
import org.dee.entity.ChatRecordZip;
import org.dee.enums.PersistenceType;
import org.dee.service.CacheChatService;
import org.dee.service.ChatContextService;
import org.dee.service.ChattingService;
import org.dee.service.SSEService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ChattingServiceImpl implements ChattingService {

    @Autowired
    private ChatClient chatClient;
    @Autowired
    private CacheChatService cacheChatService;
    @Autowired
    private ChatContextService chatContextService;

    @Autowired
    SSEService sseService;
    @Autowired
    private ToolServiceImpl toolService;


    @Override
    public String chatWithCache(String message, String conversationId, long expireSeconds) {
        // 加载上下文：获取历史对话记录和概要
        String contextPrompt = buildContextPrompt(conversationId, message);


        ChatResponse response = chatClient.prompt()
                .user(contextPrompt)
                .call()
                .chatResponse();

        String botResponse = response.getResult().getOutput().getText();

        // 保存到 Redis，设置过期时间
        cacheChatService.cacheChatMessage(conversationId, message, botResponse, expireSeconds);

        return botResponse;
    }
    @Override
    public Map<String, String> streamChatWithCache(String message, String conversationId, String userId, long expireSeconds) {
        return sseService.handleStreamChat(message, conversationId, userId,
                buildContextPrompt(conversationId,message),
                expireSeconds);
    }
    @Override
    public String chatUsingTool(String message, String conversationId, long expireSeconds) {
        String contextPrompt = buildContextPrompt(conversationId, message);

        ChatResponse response = chatClient.prompt()
                .user(contextPrompt)
                .toolCallbacks(toolService.selectEnabledToolCallbacks())
                .call()
                .chatResponse();

        return response.getResult().getOutput().getText();
    }
    @Override
    public Map<String, String> streamChatUsingTool(String message, String conversationId, String userId, long expireSeconds) {
        return sseService.handleStreamChatWithTools(message, conversationId, userId,
                buildContextPrompt(conversationId,message),
                expireSeconds);
    }
    @Override
    public void persistChatMessages(String conversationId, PersistenceType type) {
        cacheChatService.persistChatMessages(conversationId, type);
    }




    /**
     * 构建包含上下文的提示词
     *
     * @param conversationId 对话ID
     * @param currentMessage 当前用户消息
     * @return 包含上下文的完整提示词
     */
    private String buildContextPrompt(String conversationId, String currentMessage) {
        StringBuilder contextBuilder = new StringBuilder();

        // 1. 加载概要记录（ChatRecordZip）- 从数据库
        ChatRecordZip recordZip = chatContextService.getChatRecordZip(conversationId);
        if (recordZip != null && recordZip.getCompressedData() != null && !recordZip.getCompressedData().isEmpty()) {
            contextBuilder.append("[对话概要]\n");
            contextBuilder.append(recordZip.getCompressedData());
            contextBuilder.append("\n\n");
        }

        // 2. 优先从缓存加载历史对话记录
        List<ChatMessageDTO> cachedMessages = cacheChatService.getCachedChatMessages(conversationId, org.dee.dto.ChatMessageDTO.class);

        if (cachedMessages != null && !cachedMessages.isEmpty()) {
            // 从缓存加载
            contextBuilder.append("[最近对话]\n");
            for (org.dee.dto.ChatMessageDTO msg : cachedMessages) {
                contextBuilder.append("用户: ").append(msg.getUserMessage()).append("\n");
                contextBuilder.append("助手: ").append(msg.getBotResponse()).append("\n");
            }
            contextBuilder.append("\n");
        } else {
            // 缓存为空，从数据库加载
            List<ChatRecord> chatRecords = chatContextService.getChatRecords(conversationId);
            if (chatRecords != null && !chatRecords.isEmpty()) {
                contextBuilder.append("[历史对话]\n");
                for (ChatRecord record : chatRecords) {
                    contextBuilder.append("用户: ").append(record.getUserMessage()).append("\n");
                    contextBuilder.append("助手: ").append(record.getBotResponse()).append("\n");
                }
                contextBuilder.append("\n");
            }
        }

        // 3. 添加当前消息
        contextBuilder.append("[当前问题]\n");
        contextBuilder.append(currentMessage);

        return contextBuilder.toString();
    }
}
