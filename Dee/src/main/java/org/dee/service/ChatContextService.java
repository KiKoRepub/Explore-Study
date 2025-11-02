package org.dee.service;

import org.dee.entity.ChatRecord;
import org.dee.entity.ChatRecordZip;

import java.util.List;

public interface ChatContextService {

    List<ChatRecord> getChatRecords(String conversationId);

    ChatRecordZip getChatRecordZip(String conversationId);
}