package org.deepseek.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.client.McpAsyncClient;
import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;
import io.modelcontextprotocol.spec.McpClientTransport;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.Data;
import org.deepseek.callback.AMapToolCallbackProvider;
import org.springframework.ai.mcp.AsyncMcpToolCallback;
import org.springframework.ai.mcp.client.autoconfigure.NamedClientMcpTransport;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
//@Data
//@Configuration
//@ConfigurationProperties(prefix = "amap")
public class AMapMcpConfiguration {


     private String apiKey;
     private String baseUrl;
//    @Bean
    public List<NamedClientMcpTransport> aMapTransports() {
//        joyAgent
        McpClientTransport transport = HttpClientSseClientTransport.builder(baseUrl)
                .sseEndpoint("/sse?key=" + apiKey)
                .objectMapper(new ObjectMapper())
                .build();

        return Collections.singletonList(new NamedClientMcpTransport("amap", transport));
    }


}
