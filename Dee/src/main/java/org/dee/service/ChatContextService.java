package org.dee.service;

import org.dee.entity.dto.ChatMessageDTO;
import org.dee.entity.ChatRecord;
import org.dee.entity.ChatRecordZip;

import java.util.List;

public interface ChatContextService {

    List<ChatRecord> getChatRecords(String conversationId);

    List<ChatRecord> getChatRecords(String conversationId, int limit);

    /**
     * 保存聊天记录
     *
     * @param conversationId 对话ID
     * @param userMessage    用户消息
     * @param botResponse    机器人回复
     * @return 保存的记录
     */
    boolean saveChatRecord(String conversationId, String userMessage, String botResponse);

    boolean batchSaveChatRecords(String conversationId, String userId, List<ChatMessageDTO> records, String persistentTypeCode);


    /**
     * 生成对话摘要
     *
     * @param messages 聊天消息列表
     * @return 摘要文本
     */
    String generateSummary(List<ChatMessageDTO> messages);

    String buildContextPrompt(String conversationId, String userId, String currentMessage);

    ChatRecordZip getChatRecordZip(String conversationId);

    /**
     * 保存对话概要
     *
     * @param conversationId      对话ID
     * @param userId              用户ID
     * @param title               标题
     * @param compressedData      压缩数据
     * @param persistenceTypeCode 持久化类型
     * @return 保存的概要记录
     */
    boolean saveChatRecordZip(String conversationId, String userId, String title, String compressedData, String persistenceTypeCode);

}