package org.deepseek.config;

import ai.z.openapi.ZhipuAiClient;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

@Configuration
public class ChatBeanConfiguration {

    @Value("${extra.zhipu-completion.api-key}")
    private String zhiPuCompletionApiKey;

    @Bean
    public ChatMemory chatMemory() {
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(new InMemoryChatMemoryRepository())
                .maxMessages(20) // 最大消息数
                .build();// 开启内存 信息存储功能
    }


    @Bean
    public ChatClient chatClient(ChatModel deepSeekChatModel,ChatMemory memory) {
        System.out.println("model = " + deepSeekChatModel);
        return ChatClient.builder(deepSeekChatModel)
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
