package org.dee.config;

import io.modelcontextprotocol.client.McpAsyncClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;

import java.util.List;

/**
 * MCP客户端配置类
 * 用于初始化和管理MCP客户端连接
 */
@Slf4j
@Configuration
public class McpClientConfiguration {

    @Autowired(required = false)
    private List<McpAsyncClient> mcpAsyncClients;

    /**
     * 应用启动后初始化MCP客户端
     */
    @EventListener(ContextRefreshedEvent.class)
    public void initializeMcpClients() {
        if (mcpAsyncClients == null || mcpAsyncClients.isEmpty()) {
            log.warn("⚠️ 未找到任何MCP客户端配置");
            return;
        }

        log.info("========================================");
        log.info("开始初始化 {} 个MCP客户端", mcpAsyncClients.size());
        log.info("========================================");

        for (McpAsyncClient client : mcpAsyncClients) {
            try {
                log.info("正在初始化MCP客户端...");
                
                // 获取服务器信息
                var serverInfo = client.getServerInfo();
                log.info("✓ 服务器名称: {}", serverInfo.name());
                log.info("✓ 服务器版本: {}", serverInfo.version());
                
                // 获取可用工具列表
                var toolsResponse = client.listTools().block();
                if (toolsResponse != null && toolsResponse.tools() != null) {
                    int toolCount = toolsResponse.tools().size();
                    log.info("✓ 发现 {} 个可用工具:", toolCount);
                    
                    toolsResponse.tools().forEach(tool -> {
                        log.info("  - {}: {}", tool.name(), tool.description());
                    });
                } else {
                    log.warn("⚠️ 未找到任何工具");
                }
                
                log.info("----------------------------------------");
                
            } catch (Exception e) {
                log.error("❌ 初始化MCP客户端失败: {}", e.getMessage(), e);
            }
        }
        
        log.info("========================================");
        log.info("MCP客户端初始化完成");
        log.info("========================================");
    }
}
