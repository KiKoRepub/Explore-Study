package org.dee.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.dee.dto.ChatMessageDTO;
import org.dee.entity.ChatRecord;
import org.dee.entity.ChatRecordZip;
import org.dee.mapper.ChatRecordMapper;
import org.dee.mapper.ChatRecordZipMapper;
import org.dee.service.ChatRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ChatRecordServiceImpl implements ChatRecordService {

    @Autowired
    private ChatRecordMapper chatRecordMapper;

    @Autowired
    private ChatRecordZipMapper chatRecordZipMapper;

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
    public boolean batchSaveChatRecords(String conversationId,List<ChatMessageDTO> messageList) {
        List<ChatRecord> recordList = new ArrayList<>();
        for (ChatMessageDTO message : messageList) {
            ChatRecord record = new ChatRecord();
            record.setConversationId(conversationId);
            record.setUserMessage(message.getUserMessage());
            record.setBotResponse(message.getBotResponse());

            record.setCreatedAt(LocalDateTime.now());

            recordList.add(record);
        }


        int inserted = chatRecordMapper.batchInsert(recordList);
        return inserted == recordList.size();
    }

    @Override
    public boolean saveChatRecordZip(String conversationId, String title, String compressedData) {
        // 检查是否已存在该对话的概要记录
        LambdaQueryWrapper<ChatRecordZip> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ChatRecordZip::getConversationId, conversationId);
        ChatRecordZip existingRecord = chatRecordZipMapper.selectOne(queryWrapper);

        if (existingRecord != null) {
            // 更新已存在的记录
            existingRecord.setTitle(title);
            existingRecord.setCompressedData(compressedData);
            return chatRecordZipMapper.updateById(existingRecord) > 0;
        } else {
            // 插入新记录
            ChatRecordZip newRecord = new ChatRecordZip();
            newRecord.setConversationId(conversationId);
            newRecord.setTitle(title);
            newRecord.setCompressedData(compressedData);
            return chatRecordZipMapper.insert(newRecord) > 0;

        }
    }
}
