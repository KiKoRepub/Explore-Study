package org.dee.utlis;

import lombok.extern.slf4j.Slf4j;
import org.dee.sse.SSEServer;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.tool.ToolCallback;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * SSE响应助手类，用于统一处理流式响应
 */
@Slf4j
public class SSEResponseHelper {

    /**
     * 处理流式对话响应
     * 
     * @param chatClient ChatClient实例
     * @param prompt 用户提示词
     * @param userId 用户ID
     * @param conversationId 对话ID
     * @param onComplete 完成回调（可选）
     */
    public static void handleStreamResponse(ChatClient chatClient, 
                                           String prompt, 
                                           String userId, 
                                           String conversationId,
                                           Consumer<String> onComplete) {
        handleStreamResponse(chatClient, prompt, null, userId, conversationId, onComplete);
    }

    /**
     * 处理流式对话响应（带工具）
     * 
     * @param chatClient ChatClient实例
     * @param prompt 用户提示词
     * @param toolCallbacks 工具回调列表
     * @param userId 用户ID
     * @param conversationId 对话ID
     * @param onComplete 完成回调（可选）
     */
    public static void handleStreamResponse(ChatClient chatClient, 
                                           String prompt, 
                                           List<ToolCallback> toolCallbacks,
                                           String userId, 
                                           String conversationId,
                                           Consumer<String> onComplete) {
        new Thread(() -> {
            try {
                // 构建请求
                ChatClient.ChatClientRequestSpec requestSpec = chatClient.prompt().user(prompt);
                
                // 如果有工具回调，添加工具
                if (toolCallbacks != null && !toolCallbacks.isEmpty()) {
                    requestSpec = requestSpec.toolCallbacks(toolCallbacks);
                }
                
                // 使用流式API
                Flux<ChatResponse> responseFlux = requestSpec.stream().chatResponse();

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
                            // 完成后的处理
                            String botResponse = fullResponse.toString();
                            sendCompleteMessage(userId, conversationId, botResponse);
                            
                            // 执行完成回调
                            if (onComplete != null) {
                                onComplete.accept(botResponse);
                            }
                            
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
     * 发送消息块
     */
    public static void sendChunkMessage(String userId, String conversationId, String content) {
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
    public static void sendErrorMessage(String userId, String conversationId, String errorMessage) {
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
    public static void sendCompleteMessage(String userId, String conversationId, String fullResponse) {
        Map<String, Object> completeData = new HashMap<>();
        completeData.put("type", "complete");
        completeData.put("conversationId", conversationId);
        completeData.put("fullResponse", fullResponse);
        completeData.put("timestamp", System.currentTimeMillis());
        SSEServer.sendMessage(userId, "complete", completeData);
    }

    /**
     * 发送开始消息
     */
    public static void sendStartMessage(String userId, String conversationId) {
        Map<String, Object> startData = new HashMap<>();
        startData.put("type", "start");
        startData.put("conversationId", conversationId);
        startData.put("timestamp", System.currentTimeMillis());
        SSEServer.sendMessage(userId, "start", startData);
    }

    /**
     * 检查SSE连接并返回结果
     */
    public static Map<String, String> checkConnectionAndGetResult(String userId, String conversationId) {
        Map<String, String> result = new HashMap<>();
        
        if (!SSEServer.isConnected(userId)) {
            log.warn("用户未建立SSE连接: userId={}", userId);
            result.put("error", "请先建立SSE连接");
            result.put("conversationId", conversationId);
            return result;
        }
        
        result.put("status", "processing");
        result.put("conversationId", conversationId);
        result.put("message", "对话处理中，请通过SSE接收响应");
        return result;
    }
}
