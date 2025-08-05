package org.deepseek.service.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.deepseek.entity.ChatMessage;
import org.deepseek.mapper.ChatMessageMapper;
import org.deepseek.service.ChatMessageService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
@Service
@DS("mysql")
public class ChatMessageServiceImpl extends ServiceImpl<ChatMessageMapper, ChatMessage> implements ChatMessageService {


    @Override
    public boolean saveRecords(List<ChatMessage> cacheList) {
        return saveBatch(cacheList);
    }

    @Override
    public List<ChatMessage> readRecords(String conversationId) {

        return baseMapper.selectList(new LambdaQueryWrapper<ChatMessage>()
                .eq(ChatMessage::getConversationId, conversationId));

    }

    public List<ChatMessage> readRecords(String conversationId, LocalDateTime afterStamp) {
        return baseMapper.selectList(new LambdaQueryWrapper<ChatMessage>()
                .eq(ChatMessage::getConversationId, conversationId)
                .ge(ChatMessage::getCreateTime, afterStamp));
    }


}
