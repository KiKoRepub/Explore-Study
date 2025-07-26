package org.deepseek.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

public class WebSearchTool {

    @Tool(name = "web_search",description ="get info from web")
    public String getWebSearchResult(@ToolParam(description = "the msg need to search") String query) {
        return "web search result";
    }
}
