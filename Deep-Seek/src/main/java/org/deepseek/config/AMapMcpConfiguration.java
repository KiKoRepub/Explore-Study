package org.deepseek.config;

import io.modelcontextprotocol.client.McpAsyncClient;
import io.modelcontextprotocol.spec.McpSchema;
import org.springframework.ai.mcp.AsyncMcpToolCallback;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Configuration
public class AMapMcpConfiguration {

    @Autowired
    private List<McpAsyncClient> mcpAsyncClients;
    @Bean
    public ToolCallback[] aMapTools(){
        McpAsyncClient mcpClient = mcpAsyncClients.get(0);

         ToolCallback[] toolCallbacks = mcpClient.listTools().map((response) -> {
            return response.tools().stream().map((tool) -> {
                return new AsyncMcpToolCallback(mcpClient, tool);
            }).toArray(ToolCallback[]::new);
        }).block();


        return toolCallbacks;

    }




}
