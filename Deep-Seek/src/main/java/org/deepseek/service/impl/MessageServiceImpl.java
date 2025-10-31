package org.deepseek.service.impl;

import com.alibaba.fastjson.JSON;
import lombok.RequiredArgsConstructor;
import org.apache.rocketmq.client.QueryResult;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageClientIDSetter;
import org.apache.rocketmq.common.message.MessageExt;

import org.deepseek.service.MessageService;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.document.Document;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {


    private final static int QUERY_MESSAGE_MAX_NUM = 64;

    @Tool(description = "通过topic和消息id查询消息", name = "查询消息")
    @Override
    public Message queryMessageById(String nameserver, String topic, String messageId, String accessKey,
                                    String secretKey) {
        String toReturn = topic + messageId + accessKey + secretKey;
        return new Message(topic, messageId.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public Prompt createRAGPrompt(String message, List<Document> documents, int topK) {
        String prompt = """
                你是一个智能助手，你可以根据下面搜索到的内容回复用户
                ### 用户的问题是
                %s
                ### 搜索到的内容是
                %s
               """;
        prompt = String.format(prompt,message,documents.get(0).getText());

        return new Prompt(prompt);


    }

}
