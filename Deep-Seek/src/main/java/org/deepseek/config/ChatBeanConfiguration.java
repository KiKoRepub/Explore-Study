package org.deepseek.config;

import ai.z.openapi.ZhipuAiClient;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatBeanConfiguration {

    @Value("${extra.zhipu-completion.api-key}")
    private String zhiPuCompletionApiKey;




    @Bean
    public ChatClient chatClient(ChatModel ollamaChatModel, ChatMemory memory) {
        System.out.println("model = " + ollamaChatModel);
        return ChatClient.builder(ollamaChatModel)
//                .defaultAdvisors(MessageChatMemoryAdvisor.builder(memory).build()) // open memory function
                .build();
    }

    @Bean
    public ZhipuAiClient zhipuAiClient(){
       return ZhipuAiClient.builder()
                .apiKey(zhiPuCompletionApiKey)
                .build();
    }




}
