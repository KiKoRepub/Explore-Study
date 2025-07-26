package org.deepseek.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.context.annotation.Description;

@Description("天气查询工具")
public class GetWeatherTool {

    @Tool(description ="获取特定地区今天的天气信息")
    public String getWeatherInfo(@ToolParam(description = "特定的地区",required = true) String location) {
        return "the weather of " + location + "is sunny ,but it may be raining afternoon  ! ";
    }

    @Tool(description = "获取特定地区明天的天气信息")
    public String getWeatherInfoTomorrow(@ToolParam(description = "特定的地区",required = true) String location) {
        return "the weather of " + location + "is sunny tomorrow ! ";
    }

    @Tool(description = "获取特定地区后天的天气信息")
    public String getWeatherInfoAfterTomorrow(@ToolParam(description = "特定的地区",required = true) String location) {
        return "the weather of " + location + "is sunny after tomorrow ! ";
    }
}
