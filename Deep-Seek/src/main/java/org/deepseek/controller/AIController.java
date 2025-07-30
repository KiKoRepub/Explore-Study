package org.deepseek.controller;


import ai.z.openapi.ZhipuAiClient;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.image.ImageModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;

@Controller
public class AIController {

    protected static final String DEFAULT_PROMPT = "你是谁 ?";
    @Autowired
    protected ChatClient chatClient;

    protected  ChatModel deepSeekChatModel;

    public AIController( OpenAiChatModel openAiChatModel) {
        this.deepSeekChatModel = openAiChatModel;
    }


}
