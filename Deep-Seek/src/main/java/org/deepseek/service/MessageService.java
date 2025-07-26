package org.deepseek.service;

import org.apache.rocketmq.common.message.Message;
import org.springframework.ai.tool.annotation.Tool;

public interface MessageService {
    @Tool(description = "通过topic和消息id查询消息", name = "查询消息")
    Message queryMessageById(String nameserver, String topic, String messageId, String accessKey,
                             String secretKey);
}
