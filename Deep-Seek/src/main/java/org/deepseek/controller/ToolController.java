package org.deepseek.controller;

import org.deepseek.tools.GetWeatherTool;
import org.deepseek.tools.ChatHistoryManageTool;
import org.deepseek.tools.WebSearchTool;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/tool")
public class ToolController extends AIController {

    @Autowired
    ChatHistoryManageTool saveCacheGetTool;

    @Autowired
    ChatMemory chatMemory;

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

    @PostMapping("/redis-push")
    public String toolRedisPush(@RequestParam("message") String message,
    @RequestParam("conversation_id") String conversationId) {

        String templateStr = """
                当前对话的id为 {conversation_id}
                """;

        PromptTemplate template = PromptTemplate.builder()
                .template(templateStr)
                .build();
        Prompt userPrompt = template.create(Map.of("conversation_id", conversationId));

        String content = chatClient.prompt(userPrompt)
                .user(message)
                .advisors(MessageChatMemoryAdvisor.builder(chatMemory).conversationId(conversationId).build())
                .tools(saveCacheGetTool)
                .call()
                .content();


        System.out.println("AI 返回的结果为" + content);

        return content;
    }





}
