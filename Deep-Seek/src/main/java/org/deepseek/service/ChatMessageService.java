package org.deepseek.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.deepseek.entity.ChatMessage;

import java.time.LocalDateTime;
import java.util.List;

//缓存持久化服务
public interface ChatMessageService extends IService<ChatMessage> {
    boolean saveRecords(List<ChatMessage> cacheList);

    List<ChatMessage> readRecords(String conversationId);

    List<ChatMessage> readRecords(String conversationId, LocalDateTime afterStamp);

    }
