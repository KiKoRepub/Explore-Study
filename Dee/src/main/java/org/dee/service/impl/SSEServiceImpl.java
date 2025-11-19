package org.dee.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.dee.enums.ErrorCodeEnum;
import org.dee.service.*;
import org.dee.sse.SSEServer;
import org.dee.utlis.ChatUtils;
import org.dee.entity.vo.ResultBean;
import org.dee.entity.vo.SSEMessageVo;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
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
    @Autowired
    private MCPService mcpService;

    @Override
    public ResultBean handleStreamChat(String message, String conversationId, String userId,String contextPrompt, long expireSeconds) {
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
    public ResultBean handleStreamChatWithTools(String message, String conversationId, String userId,String contextPrompt, long expireSeconds) {
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

    @Override
    public ResultBean handleStreamChatWithMcpTools(String message, String conversationId, String userId,
                                                   String contextPrompt, long expireSeconds, List<String> mcpNames) {
        List<ToolCallback> mcpCallbackList = mcpService.getUserSelectedToolCallbacks(mcpNames);

        processStreamChatAsync(contextPrompt,message, conversationId, userId, expireSeconds, mcpCallbackList);

        return createProcessingResult(conversationId);
    }

    /**
     * 异步处理流式对话
     */
    private void processStreamChatAsync(String contextPrompt,String message, String conversationId, String userId,
                                       long expireSeconds, List<ToolCallback> toolCallbacks) {
        String conversationKey = ChatUtils.buildConversationKey(conversationId,userId);
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
                            cacheChatService.cacheChatMessage(conversationKey, message, botResponse, expireSeconds);

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
        SSEServer.sendMessage(userId, "error", new SSEMessageVo(
                userId,
                conversationId,
                "error",
                errorMessage,
                LocalDateTime.now()
        ));
    }

    /**
     * 发送完成消息
     */
    private void sendCompleteMessage(String userId, String conversationId, String fullResponse) {

        SSEServer.sendMessage(userId, "complete",
                new SSEMessageVo(userId,
                    conversationId,
                        "complete",
                        fullResponse,
                        LocalDateTime.now())
        );
    }

    /**
     * 创建错误结果
     */
    private ResultBean createErrorResult(String error, String conversationId) {
        Map<String, String> result = new HashMap<>();
        result.put("status", "error");
        result.put("conversationId", conversationId);
        result.put("message", error);
        return ResultBean.error(ErrorCodeEnum.SERVICE_ERROR,error,result);
    }

    /**
     * 创建处理中结果
     */
    private ResultBean createProcessingResult(String conversationId) {
        Map<String, String> result = new HashMap<>();
        result.put("status", "processing");
        result.put("conversationId", conversationId);
        result.put("message", "对话处理中，请通过SSE接收响应");
        return ResultBean.success(result);
    }
}
