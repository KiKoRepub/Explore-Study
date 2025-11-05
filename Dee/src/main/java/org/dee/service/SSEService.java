package org.dee.service;

import java.util.Map;

/**
 * SSE 流式对话服务接口
 */
public interface SSEService {

    /**
     * 处理流式对话（带记忆和上下文）
     * 
     * @param message 用户消息
     * @param conversationId 对话ID
     * @param userId 用户ID
     * @param expireSeconds 缓存过期时间
     * @return 处理结果
     */
    Map<String, String> handleStreamChat(String message, String conversationId, String userId,String contextPrompt, long expireSeconds);

    /**
     * 处理流式对话（带工具）
     * 
     * @param message 用户消息
     * @param conversationId 对话ID
     * @param userId 用户ID
     * @param expireSeconds 缓存过期时间
     * @return 处理结果
     */
    Map<String, String> handleStreamChatWithTools(String message, String conversationId, String userId,String contextPrompt, long expireSeconds);
}
