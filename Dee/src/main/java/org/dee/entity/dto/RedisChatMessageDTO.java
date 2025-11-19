package org.dee.entity.dto;


import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
public class RedisChatMessageDTO {

    @ApiModelProperty("使用的工具集")
    private List<String> toolCalls;
    @ApiModelProperty("用户消息")
    private String  userMessage;
    @ApiModelProperty("机器人回复")
    private String  botResponse;

    public RedisChatMessageDTO(String userMessage, String botResponse) {
        this.toolCalls = new ArrayList<>();
        this.userMessage = userMessage;
        this.botResponse = botResponse;
    }

    public RedisChatMessageDTO(List<String> toolCalls, String userMessage, String botResponse) {
        this.toolCalls = toolCalls;
        this.userMessage = userMessage;
        this.botResponse = botResponse;
    }

}
