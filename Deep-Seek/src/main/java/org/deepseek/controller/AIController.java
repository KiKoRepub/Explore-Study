package org.deepseek.controller;


import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;

@Controller
public class AIController {

    protected static final String DEFAULT_PROMPT = "你是谁 ?";
    @Autowired
    protected ChatClient chatClient;
    @Autowired
//    @Qualifier("deepSeekChatModel")
    @Qualifier("ollamaChatModel")
    protected  ChatModel deepSeekChatModel;



}
