package org.dee.service.impl;

import org.dee.enums.PersistenceType;
import org.dee.service.*;
import org.dee.utlis.ChatUtils;
import org.dee.entity.vo.ResultBean;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChattingServiceImpl implements ChattingService {

    @Autowired
    private ChatClient chatClient;
    @Autowired
    private CacheChatService cacheChatService;
    @Autowired
    private ChatContextService chatContextService;

    @Autowired
    private SSEService sseService;
    @Autowired
    private ToolService toolService;
    @Autowired
    private MCPService mcpService;


    @Override
    public ResultBean chatWithCache(String message, String conversationId, String userId, long expireSeconds) {
        // 加载上下文：获取历史对话记录和概要
        String contextPrompt = chatContextService.buildContextPrompt(conversationId, userId, message);


        ChatResponse response = chatClient.prompt()
                .user(contextPrompt)
                .call()
                .chatResponse();

        String botResponse = response.getResult().getOutput().getText();

        String conversationKey = ChatUtils.buildConversationKey(conversationId, userId);

        // 保存到 Redis，设置过期时间
        cacheChatService.cacheChatMessage(conversationKey, message, botResponse, expireSeconds);

        return ResultBean.success(botResponse);
    }
    @Override
    public ResultBean streamChatWithCache(String message, String conversationId, String userId, long expireSeconds) {
        return sseService.handleStreamChat(message, conversationId, userId,
                chatContextService.buildContextPrompt(conversationId, userId, message),
                expireSeconds);
    }
    @Override
    public ResultBean chatUsingTool(String message, String conversationId, String userId, long expireSeconds) {
        String contextPrompt = chatContextService.buildContextPrompt(conversationId, userId, message);

        ChatResponse response = chatClient.prompt()
                .user(contextPrompt)
                .toolCallbacks(toolService.selectEnabledToolCallbacks())
                .call()
                .chatResponse();

        String botResponse = response.getResult().getOutput().getText();
        
        String conversationKey = ChatUtils.buildConversationKey(conversationId, userId);
        cacheChatService.cacheChatMessage(conversationKey, message, botResponse, expireSeconds);

        return ResultBean.success(botResponse);
    }
    @Override
    public ResultBean streamChatUsingTool(String message, String conversationId, String userId, long expireSeconds) {
        return sseService.handleStreamChatWithTools(message, conversationId, userId,
                chatContextService.buildContextPrompt(conversationId, userId, message),
                expireSeconds);
    }

    @Override
    public ResultBean chatUsingMcpTool(String message, String conversationId, String userId,
                                             long expireSeconds, List<String> mcpNames){
        String contextPrompt = chatContextService.buildContextPrompt(conversationId, userId, message);

        ChatResponse response = chatClient.prompt()
                .user(contextPrompt)
                .toolCallbacks(mcpService.getUserSelectedToolCallbacks(mcpNames))
                .call()
                .chatResponse();

        String botResponse = response.getResult().getOutput().getText();

        String conversationKey = ChatUtils.buildConversationKey(conversationId, userId);
        cacheChatService.cacheChatMessage(conversationKey, message, botResponse, expireSeconds);

        return ResultBean.success(botResponse);
    }
    @Override
    public ResultBean streamChatUsingMcpTool(String message, String conversationId, String userId, long expireSeconds, List<String> mcpNames){
        return sseService.handleStreamChatWithMcpTools(message, conversationId, userId,
                chatContextService.buildContextPrompt(conversationId, userId, message),
                expireSeconds, mcpNames);
    }

    @Override
    public void persistChatMessages(String conversationId, String userId, PersistenceType type) {
        cacheChatService.persistChatMessages(conversationId, userId, type);
    }





}
