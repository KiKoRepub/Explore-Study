package org.dee.config;

import io.modelcontextprotocol.client.McpAsyncClient;
import lombok.extern.slf4j.Slf4j;
import org.dee.callBack.MyMcpToolCallBackProvider;
import org.springframework.ai.mcp.AsyncMcpToolCallback;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * MCP工具配置类
 * 负责将MCP客户端的工具注册为Spring AI的ToolCallback
 */
@Slf4j
@Configuration
public class MCPBeanConfiguration {

    @Autowired(required = false)
    private List<McpAsyncClient> mcpAsyncClients;

    /**
     * 创建MCP工具回调提供者
     * 将所有MCP客户端的工具转换为ToolCallback并注册
     * 
     * @return ToolCallbackProvider实例
     */
    @Bean
    public Map<String,MyMcpToolCallBackProvider>  mcpToolCallbackProviderMap() {
        log.info("初始化MCP工具回调提供者");
        Map<String,MyMcpToolCallBackProvider> providerMap = new HashMap<>();

        if (mcpAsyncClients == null || mcpAsyncClients.isEmpty()) {
            log.warn("未找到任何MCP客户端，返回空的Map");
            return providerMap;
        }

        for (McpAsyncClient client : mcpAsyncClients){
            AsyncMcpToolCallback[] clientCallBacks = client.listTools().map((response) -> {
                return response.tools().stream().map((tool) -> {
//                new McpSchema.Tool()
                    return new AsyncMcpToolCallback(client, tool);
                }).toArray(AsyncMcpToolCallback[]::new);
            }).block();

            if (clientCallBacks == null){
                log.warn("MCP客户端 {} 未返回任何工具", client.getServerInfo().name());
                continue;
            }

            providerMap.put(client.getServerInfo().name(), new MyMcpToolCallBackProvider(clientCallBacks));

        }

        return providerMap;
    }
    
    /**
     * 获取所有MCP工具的名称列表
     * 
     * @return 工具名称列表
     */
    @Bean
    public List<String> mcpToolNames() {
        if (mcpAsyncClients == null || mcpAsyncClients.isEmpty()) {
            return new ArrayList<>();
        }
        
        return mcpAsyncClients.stream()
                .map(client -> client.getServerInfo().name())
                .collect(Collectors.toList());
    }

}
