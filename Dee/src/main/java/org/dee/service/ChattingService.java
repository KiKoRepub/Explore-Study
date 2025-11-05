package org.dee.service;

import org.dee.enums.PersistenceType;

import java.util.Map;

public interface ChattingService {
    String chatWithCache(String message, String conversationId, long expireSeconds);
    Map<String, String> streamChatWithCache(String message, String conversationId, String userId, long expireSeconds);


    String chatUsingTool(String message, String conversationId, long expireSeconds);
    Map<String, String> streamChatUsingTool(String message, String conversationId, String userId, long expireSeconds);

    void persistChatMessages(String conversationId, PersistenceType type);

}
