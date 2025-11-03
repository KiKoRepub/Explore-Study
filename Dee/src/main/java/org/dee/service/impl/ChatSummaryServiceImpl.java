package org.dee.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.dee.dto.ChatMessageDTO;
import org.dee.dto.ChatSummaryDTO;
import org.dee.service.ChatSummaryService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 聊天记录摘要生成服务实现
 */
@Slf4j
@Service
public class ChatSummaryServiceImpl implements ChatSummaryService {

    @Autowired
    private ChatClient chatClient;

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
