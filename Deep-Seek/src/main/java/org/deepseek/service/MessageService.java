package org.deepseek.service;

import org.apache.rocketmq.common.message.Message;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.document.Document;
import org.springframework.ai.tool.annotation.Tool;

import java.util.List;

public interface MessageService {
    @Tool(description = "通过topic和消息id查询消息", name = "查询消息")
    Message queryMessageById(String nameserver, String topic, String messageId, String accessKey,
                             String secretKey);


    Prompt createRAGPrompt(String message, List<Document> documents,int topK);
}
