package org.dee.service;

import org.dee.dto.ChatMessageDTO;
import org.springframework.ai.chat.messages.Message;

import java.util.List;

/**
 * 聊天记录摘要生成服务
 */
public interface ChatSummaryService {

    /**
     * 生成对话摘要
     * @param messages 聊天消息列表
     * @return 摘要文本
     */
    String generateSummary(List<ChatMessageDTO> messages);
}
