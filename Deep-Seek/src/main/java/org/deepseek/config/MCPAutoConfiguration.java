package org.deepseek.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;
import io.modelcontextprotocol.spec.McpClientTransport;
import org.deepseek.service.MessageService;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.mcp.client.autoconfigure.NamedClientMcpTransport;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.List;

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
    @Value("${amap.api-key}")
    private String apiKey;
    @Value("${amap.base-url}")
    private String baseUrl;
    @Bean
    public List<NamedClientMcpTransport> aMapTransports() {
        McpClientTransport transport = HttpClientSseClientTransport.builder(baseUrl)
                .sseEndpoint("/sse?key=" + apiKey)
                .objectMapper(new ObjectMapper())
                .build();

        return Collections.singletonList(new NamedClientMcpTransport("amap", transport));
    }



}

