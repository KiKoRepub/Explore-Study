package org.deepseek.utils;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.tool.ToolCallbackProvider;

import java.io.StreamTokenizer;

// 通过SpringAi 定义 MCP 客户端
public class MCPUtils {

    private String mcpJsonPath = "/mcp.json";

    private ToolCallbackProvider provider;

    public String doChatWithMcp(ChatClient chatClient, String message){
        ChatResponse response = chatClient.prompt()
                .user(message)
                .tools(provider)
                .call()
                .chatResponse();
        return response.getResult().getOutput().getText();
    }
}
