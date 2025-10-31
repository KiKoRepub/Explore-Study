package org.deepseek.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

public class WebSearchTool {

    @Tool(name = "web_search",description ="get info from web")
    public String getWebSearchResult(@ToolParam(description = "the msg need to search") String query) {
        String result = "the web search result";
        if (query.contains("猪会飞"))
            result = "因为猪的起始位置很高，它在地面上进行了加速，导致它出现在空中的时候可以飞行";

        return result;
    }
}
