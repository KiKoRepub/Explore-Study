package org.dee.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.dee.entity.ChatRecord;
import org.dee.entity.ChatRecordZip;
import org.dee.mapper.ChatRecordMapper;
import org.dee.mapper.ChatRecordZipMapper;
import org.dee.service.ChatContextService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChatContextServiceImpl implements ChatContextService {

    @Autowired
    private ChatRecordMapper chatRecordMapper;

    @Autowired
    private ChatRecordZipMapper chatRecordZipMapper;

    @Override
    public List<ChatRecord> getChatRecords(String conversationId) {
        LambdaQueryWrapper<ChatRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ChatRecord::getConversationId, conversationId)
                    .orderByAsc(ChatRecord::getCreatedAt);
        return chatRecordMapper.selectList(queryWrapper);
    }

    @Override
    public ChatRecordZip getChatRecordZip(String conversationId) {
        LambdaQueryWrapper<ChatRecordZip> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ChatRecordZip::getConversationId, conversationId);
        return chatRecordZipMapper.selectOne(queryWrapper);
    }
}