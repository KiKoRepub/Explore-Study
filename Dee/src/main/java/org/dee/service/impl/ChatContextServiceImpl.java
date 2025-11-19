package org.dee.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.dee.entity.dto.ChatMessageDTO;
import org.dee.entity.ChatRecord;
import org.dee.entity.ChatRecordZip;
import org.dee.mapper.ChatRecordMapper;
import org.dee.mapper.ChatRecordZipMapper;
import org.dee.service.CacheChatService;
import org.dee.service.ChatContextService;
import org.dee.utlis.ChatUtils;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
@Slf4j
@Service
public class ChatContextServiceImpl implements ChatContextService {

    @Autowired
    private ChatClient chatClient;
    @Autowired
    private ChatRecordMapper chatRecordMapper;

    @Autowired
    private ChatRecordZipMapper chatRecordZipMapper;



    private final CacheChatService cacheChatService;

    public ChatContextServiceImpl(@Lazy CacheChatService cacheChatService) {
        this.cacheChatService = cacheChatService;
    }
    private static final int MAX_CONTEXT_MESSAGES = 5; // 最大上下文消息数

    public List<ChatRecord> getChatRecords(String conversationId) {
        LambdaQueryWrapper<ChatRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ChatRecord::getConversationId, conversationId)
                    .orderByAsc(ChatRecord::getCreatedAt);
        return chatRecordMapper.selectList(queryWrapper);
    }
    @Override
    public List<ChatRecord> getChatRecords(String conversationId, int limit) {
        LambdaQueryWrapper<ChatRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ChatRecord::getConversationId, conversationId)
                    .orderByAsc(ChatRecord::getCreatedAt)
                    .last("LIMIT " + limit);
        return chatRecordMapper.selectList(queryWrapper);
    }
    @Override
    public boolean saveChatRecord(String conversationId, String userMessage, String botResponse) {
        ChatRecord record = new ChatRecord();
        record.setConversationId(conversationId);
        record.setUserMessage(userMessage);
        record.setBotResponse(botResponse);
        record.setCreatedAt(LocalDateTime.now());

        return chatRecordMapper.insert(record) > 0;
    }
    @Override
    public boolean batchSaveChatRecords(String conversationId,String userId, List<ChatMessageDTO> messageList, String persistentTypeCode){
        List<ChatRecord> recordList = new ArrayList<>();
        for (ChatMessageDTO message : messageList) {
            ChatRecord record = new ChatRecord();
            record.setConversationId(conversationId);
            record.setUserId(userId);
            record.setUserMessage(message.getUserMessage());
            record.setBotResponse(message.getBotResponse());

            record.setCreatedAt(LocalDateTime.now());
            record.setPersistenceTypeCode(persistentTypeCode);

            recordList.add(record);
        }


        int inserted = chatRecordMapper.batchInsert(recordList);
        return inserted == recordList.size();
    }
    @Override
    public ChatRecordZip getChatRecordZip(String conversationId) {
        LambdaQueryWrapper<ChatRecordZip> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ChatRecordZip::getConversationId, conversationId);
        return chatRecordZipMapper.selectOne(queryWrapper);
    }

    @Override
    public boolean saveChatRecordZip(String conversationId, String userId, String title, String compressedData, String persistenceTypeCode) {
        // 检查是否已存在该对话的概要记录
        LambdaQueryWrapper<ChatRecordZip> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ChatRecordZip::getConversationId, conversationId);
        ChatRecordZip existingRecord = chatRecordZipMapper.selectOne(queryWrapper);

        LocalDateTime now = LocalDateTime.now();


        if (existingRecord != null) {
            // 更新已存在的记录
            existingRecord.setUserId(userId);
            existingRecord.setTitle(title);
            existingRecord.setCompressedData(compressedData);
            existingRecord.setPersistenceTypeCode(persistenceTypeCode);
            existingRecord.setPersistenceTime(now);
            return chatRecordZipMapper.updateById(existingRecord) > 0;
        } else {
            // 插入新记录
            ChatRecordZip newRecord = new ChatRecordZip();
            newRecord.setConversationId(conversationId);
            newRecord.setUserId(userId);
            newRecord.setTitle(title);
            newRecord.setCompressedData(compressedData);
            newRecord.setPersistenceTypeCode(persistenceTypeCode);
            newRecord.setPersistenceTime(now);
            return chatRecordZipMapper.insert(newRecord) > 0;
        }
    }


    @Override
    public String generateSummary(List<ChatMessageDTO> messages) {
        if (messages == null || messages.isEmpty()) {
            return "空对话";
        }

        // 构建对话历史文本
        StringBuilder conversationText = new StringBuilder();
        conversationText.append("请对以下对话进行简洁的摘要总结（200字以内）：\n\n");

        for (ChatMessageDTO message : messages) {
            conversationText.append("用户: ").append(message.getUserMessage()).append("\n");
            conversationText.append("助手: ").append(message.getBotResponse()).append("\n\n");
        }

        try {
            // 使用 AI 生成摘要
            String summary = chatClient.prompt()
                    .user(conversationText.toString())
                    .call()
                    .content();

            // 验证摘要是否有效
            if (summary != null && !summary.trim().isEmpty() && summary.length() > 10) {
                return summary.trim();
            } else {
                log.warn("AI 生成的摘要无效，使用简单摘要");
                return generateSimpleSummary(messages);
            }
        } catch (Exception e) {
            // 如果 AI 生成失败，返回简单摘要
            log.error("AI 生成摘要失败", e);
            return generateSimpleSummary(messages);
        }
    }
    /**
     * 构建包含上下文的提示词
     *
     * @param conversationId 对话ID
     * @param currentMessage 当前用户消息
     * @return 包含上下文的完整提示词
     */
    @Override
    public String buildContextPrompt(String conversationId,String userId,String currentMessage) {
        StringBuilder contextBuilder = new StringBuilder();

        // 1. 加载概要记录（ChatRecordZip）- 从数据库
        ChatRecordZip recordZip = getChatRecordZip(conversationId);
        if (recordZip != null && recordZip.getCompressedData() != null && !recordZip.getCompressedData().isEmpty()) {
            contextBuilder.append("[对话概要]\n");
            contextBuilder.append(recordZip.getCompressedData());
            contextBuilder.append("\n\n");
        }

        // 2. 优先从缓存加载历史对话记录
        String conversationKey = ChatUtils.buildConversationKey(conversationId,userId);
        List<ChatMessageDTO> cachedMessages = cacheChatService.getCachedChatMessages(conversationKey, ChatMessageDTO.class);

        if (cachedMessages != null && !cachedMessages.isEmpty()) {
            // 从缓存加载
            contextBuilder.append("[最近对话]\n");
            for (ChatMessageDTO msg : cachedMessages) {
                contextBuilder.append("用户: ").append(msg.getUserMessage()).append("\n");
                contextBuilder.append("助手: ").append(msg.getBotResponse()).append("\n");
            }
            contextBuilder.append("\n");
        } else {
            // 缓存为空，从数据库加载
            List<ChatRecord> chatRecords = getChatRecords(conversationId,MAX_CONTEXT_MESSAGES);
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
    /**
     * 生成简单摘要（备用方案）
     */
    private String generateSimpleSummary(List<ChatMessageDTO> messages) {
        int messageCount = messages.size();
        String firstUserMessage = messages.get(0).getUserMessage();

        // 截取前50个字符
        String preview = firstUserMessage.length() > 50
                ? firstUserMessage.substring(0, 50) + "..."
                : firstUserMessage;

        return String.format("对话包含 %d 条消息，首条消息: %s", messageCount, preview);
    }
}