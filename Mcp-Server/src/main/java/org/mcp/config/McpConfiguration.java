package org.mcp.config;

import org.mcp.tool.BookTools;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Configuration
public class McpConfiguration {


    @Bean
    public ToolCallbackProvider bookTools(BookTools bookTools) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(bookTools)
                .build();
    }


}
