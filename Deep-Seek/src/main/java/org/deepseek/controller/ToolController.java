package org.deepseek.controller;

import lombok.RequiredArgsConstructor;
import org.deepseek.tools.GetWeatherTool;
import org.deepseek.tools.WebSearchTool;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tool")
public class ToolController extends AIController {



    @GetMapping("/web-push")
    public String toolPush(@RequestParam("message") String message) {
        // 使用 tools 方法 填入 需要使用的工具
        // 工具需要满足至少有一个方法标注了 @tool,然后参数有 @toolParam 注解
        // AI 会尝试调用工具方法来实现结果(AI自己选择)
        ChatResponse response = chatClient
                .prompt()
                .user(message)
                .tools(new WebSearchTool())
                .call()
                .chatResponse();
        return response.getResult().getOutput().getText();
    }

    @GetMapping("/weather-push")
    public String toolWeatherPush(@RequestParam("message") String message) {
        System.out.println("chatClient = " + chatClient);

        String content = chatClient.prompt()
                .user(message)
                .tools(new GetWeatherTool())
                .call()
                .content();

        System.out.println("AI 返回的结果为" + content);

        return content;
    }
}
