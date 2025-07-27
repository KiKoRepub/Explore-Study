package org.mcp.config;

import org.mcp.tool.BookTools;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class McpConfiguration {


    @Bean
    public ToolCallbackProvider weatherTools(BookTools bookTools) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(bookTools)
                .build();
    }

}
