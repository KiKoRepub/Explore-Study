package org.dee.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.dee.service.CacheChatService;
import org.dee.service.ChatContextService;
import org.dee.service.SSEService;
import org.dee.service.ToolService;
import org.dee.sse.SSEServer;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * SSE 流式对话服务实现
 */
@Slf4j
@Service
public class SSEServiceImpl implements SSEService {

    @Autowired
    private ChatClient chatClient;

    @Autowired
    private CacheChatService cacheChatService;

    @Autowired
    private ChatContextService chatContextService;

    @Autowired
    private ToolService toolService;

    @Override
    public Map<String, String> handleStreamChat(String message, String conversationId, String userId,String contextPrompt, long expireSeconds) {
        log.info("开始流式对话: conversationId={}, userId={}", conversationId, userId);

        // 检查SSE连接是否存在
        if (!SSEServer.isConnected(userId)) {
            log.warn("用户未建立SSE连接: userId={}", userId);
            return createErrorResult("请先建立SSE连接", conversationId);
        }

        // 异步处理对话
        processStreamChatAsync(contextPrompt,message, conversationId, userId, expireSeconds, null);

        // 立即返回处理中状态
        return createProcessingResult(conversationId);
    }

    @Override
    public Map<String, String> handleStreamChatWithTools(String message, String conversationId, String userId,String contextPrompt, long expireSeconds) {
        log.info("开始工具流式对话: conversationId={}, userId={}", conversationId, userId);

        // 检查SSE连接是否存在
        if (!SSEServer.isConnected(userId)) {
            log.warn("用户未建立SSE连接: userId={}", userId);
            return createErrorResult("请先建立SSE连接", conversationId);
        }

        // 加载启用的工具
        List<ToolCallback> toolCallbacks = toolService.selectEnabledToolCallbacks();

        // 异步处理对话
        processStreamChatAsync(contextPrompt,message, conversationId, userId, expireSeconds, toolCallbacks);

        // 立即返回处理中状态
        return createProcessingResult(conversationId);
    }

    /**
     * 异步处理流式对话
     */
    private void processStreamChatAsync(String contextPrompt,String message, String conversationId, String userId,
                                       long expireSeconds, List<ToolCallback> toolCallbacks) {
        new Thread(() -> {
            try {

                // 构建请求
                ChatClient.ChatClientRequestSpec promptSpec = chatClient.prompt().user(contextPrompt);

                // 如果有工具回调，添加工具
                if (toolCallbacks != null && !toolCallbacks.isEmpty()) {
                    promptSpec = promptSpec.toolCallbacks(toolCallbacks);
                }

                // 使用流式API
                Flux<ChatResponse> responseFlux = promptSpec.stream().chatResponse();

                StringBuilder fullResponse = new StringBuilder();

                // 逐块发送响应
                responseFlux.subscribe(
                        response -> {
                            String content = response.getResult().getOutput().getText();
                            fullResponse.append(content);

                            // 通过SSE发送消息块
                            sendChunkMessage(userId, conversationId, content);
                        },
                        error -> {
                            log.error("流式对话出错: conversationId={}, error={}", conversationId, error.getMessage());
                            sendErrorMessage(userId, conversationId, error.getMessage());
                        },
                        () -> {
                            // 完成后保存到缓存
                            String botResponse = fullResponse.toString();
                            cacheChatService.cacheChatMessage(conversationId, message, botResponse, expireSeconds);

                            // 发送完成信号
                            sendCompleteMessage(userId, conversationId, botResponse);

                            log.info("流式对话完成: conversationId={}", conversationId);
                        }
                );
            } catch (Exception e) {
                log.error("流式对话异常: conversationId={}, error={}", conversationId, e.getMessage(), e);
                sendErrorMessage(userId, conversationId, e.getMessage());
            }
        }).start();
    }

    /**
     * 构建包含上下文的提示词
     */
    private String buildContextPrompt(String conversationId, String currentMessage) {
        StringBuilder contextBuilder = new StringBuilder();

        // 1. 加载概要记录（ChatRecordZip）- 从数据库
        var recordZip = chatContextService.getChatRecordZip(conversationId);
        if (recordZip != null && recordZip.getCompressedData() != null && !recordZip.getCompressedData().isEmpty()) {
            contextBuilder.append("[对话概要]\n");
            contextBuilder.append(recordZip.getCompressedData());
            contextBuilder.append("\n\n");
        }

        // 2. 优先从缓存加载历史对话记录
        var cachedMessages = cacheChatService.getCachedChatMessages(conversationId, org.dee.dto.ChatMessageDTO.class);

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
            var chatRecords = chatContextService.getChatRecords(conversationId);
            if (chatRecords != null && !chatRecords.isEmpty()) {
                contextBuilder.append("[历史对话]\n");
                for (var record : chatRecords) {
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

    /**
     * 发送消息块
     */
    private void sendChunkMessage(String userId, String conversationId, String content) {
        Map<String, Object> data = new HashMap<>();
        data.put("type", "chunk");
        data.put("content", content);
        data.put("conversationId", conversationId);
        data.put("timestamp", System.currentTimeMillis());
        SSEServer.sendMessage(userId, "message", data);
    }

    /**
     * 发送错误消息
     */
    private void sendErrorMessage(String userId, String conversationId, String errorMessage) {
        Map<String, Object> errorData = new HashMap<>();
        errorData.put("type", "error");
        errorData.put("message", errorMessage);
        errorData.put("conversationId", conversationId);
        errorData.put("timestamp", System.currentTimeMillis());
        SSEServer.sendMessage(userId, "error", errorData);
    }

    /**
     * 发送完成消息
     */
    private void sendCompleteMessage(String userId, String conversationId, String fullResponse) {
        Map<String, Object> completeData = new HashMap<>();
        completeData.put("type", "complete");
        completeData.put("conversationId", conversationId);
        completeData.put("fullResponse", fullResponse);
        completeData.put("timestamp", System.currentTimeMillis());
        SSEServer.sendMessage(userId, "complete", completeData);
    }

    /**
     * 创建错误结果
     */
    private Map<String, String> createErrorResult(String error, String conversationId) {
        Map<String, String> result = new HashMap<>();
        result.put("error", error);
        result.put("conversationId", conversationId);
        return result;
    }

    /**
     * 创建处理中结果
     */
    private Map<String, String> createProcessingResult(String conversationId) {
        Map<String, String> result = new HashMap<>();
        result.put("status", "processing");
        result.put("conversationId", conversationId);
        result.put("message", "对话处理中，请通过SSE接收响应");
        return result;
    }
}
