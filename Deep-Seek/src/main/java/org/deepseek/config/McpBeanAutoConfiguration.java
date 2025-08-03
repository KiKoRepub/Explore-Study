package org.deepseek.config;

import io.modelcontextprotocol.client.McpAsyncClient;
import org.deepseek.callback.AMapToolCallbackProvider;
import org.springframework.ai.mcp.AsyncMcpToolCallback;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

//@Configuration
public class McpBeanAutoConfiguration {
//    @Autowired
    private List<McpAsyncClient> mcpAsyncClients;

//    @Bean
    public ToolCallbackProvider aMapToolCallbackProvider(){

        McpAsyncClient mcpClient = mcpAsyncClients.get(0);
        ToolCallback[] toolCallbacks = mcpClient.listTools().map((response) -> {
            return response.tools().stream().map((tool) -> {
                return new AsyncMcpToolCallback(mcpClient, tool);
            }).toArray(ToolCallback[]::new);
        }).block();

        return new AMapToolCallbackProvider(toolCallbacks);
    }
}
