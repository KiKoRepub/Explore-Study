package org.deepseek.config;

import org.deepseek.service.MessageService;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MCPAutoConfiguration {

//    @Bean
//    public ToolCallbackProvider myTools(MessageService messageServic e) {
//        return MethodToolCallbackProvider
//                .builder()
//                .toolObjects(messageService)
//                .build();
//    }
//    @Bean
//    public MessageChatMemoryAdvisor messageCh atMemoryAdvisor() {
//        return new MessageChatMemoryAdvisor();
//    }
}

