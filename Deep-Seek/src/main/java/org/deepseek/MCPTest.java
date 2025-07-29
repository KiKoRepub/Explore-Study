package org.deepseek;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.transport.WebFluxSseClientTransport;
import org.springframework.web.reactive.function.client.WebClient;

public class MCPTest {
    public static void main(String[] args) {

        var transport = new WebFluxSseClientTransport(
                // 配置WebClient基础URL
                WebClient.builder().baseUrl("http://localhost:8080")
        );

// 构建同步MCP客户端
        var client = McpClient.sync(transport).build();

// 初始化客户端连接
        client.initialize();



    }


}
