package org.deepseek.config;

import ai.z.openapi.ZhipuAiClient;
import org.deepseek.model.DeepSeekChatModel;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class ChatBeanConfiguration {

    @Value("${extra.zhipu-completion.api-key}")
    private String zhiPuCompletionApiKey;

    @Bean
    public ChatMemory chatMemory() {
        return MessageWindowChatMemory.builder().build();// 开启内存 信息存储功能
    }


    @Bean
    public ChatClient chatClient(ChatModel model) {
        System.out.println("model = " + model);
        return ChatClient.builder(model)
                .build();
    }

    @Bean
    public ZhipuAiClient zhipuAiClient(){
       return ZhipuAiClient.builder()
                .apiKey(zhiPuCompletionApiKey)
                .build();
    }

}
