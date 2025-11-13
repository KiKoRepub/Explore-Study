package org.dee.service;

import org.dee.enums.PersistenceType;
import org.dee.vo.ResultBean;

import java.util.List;
import java.util.Map;

public interface ChattingService {
    ResultBean chatWithCache(String message, String conversationId, String userId, long expireSeconds);
    ResultBean streamChatWithCache(String message, String conversationId, String userId, long expireSeconds);


    ResultBean chatUsingTool(String message, String conversationId, String userId, long expireSeconds);
    ResultBean streamChatUsingTool(String message, String conversationId, String userId, long expireSeconds);
    ResultBean chatUsingMcpTool(String message, String conversationId, String userId, long expireSeconds, List<String> mcpNames);
    ResultBean streamChatUsingMcpTool(String message, String conversationId, String userId, long expireSeconds, List<String> mcpNames);

    void persistChatMessages(String conversationId, String userId, PersistenceType type);

}
