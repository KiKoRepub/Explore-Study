package org.dee.tools;

import org.dee.annotions.MyTool;
import org.springframework.ai.tool.annotation.Tool;

/**
 * 天气查询工具示例
 */
@MyTool("天气查询工具")
public class WeatherTool {

    @Tool(description = "查询指定城市的当前天气")
    public String getCurrentWeather(String city) {
        // 这里应该调用真实的天气 API
        // 目前返回模拟数据
        return String.format("城市：%s\n天气：晴天\n温度：20°C\n湿度：60%%\n风力：3级", city);
    }

    @Tool(description = "查询指定城市未来几天的天气预报")
    public String getWeatherForecast(String city, int days) {
        // 这里应该调用真实的天气预报 API
        // 目前返回模拟数据
        StringBuilder forecast = new StringBuilder();

        forecast.append(String.format("城市：%s\n未来%d天天气预报：\n", city, days));
        
        for (int i = 1; i <= days; i++) {
            forecast.append(String.format("第%d天：晴转多云，温度 18-25°C\n", i));
        }
        
        return forecast.toString();
    }
}
