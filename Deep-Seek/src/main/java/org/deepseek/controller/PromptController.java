package org.deepseek.controller;

import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/prompt")
public class PromptController extends AIController {




    @PostMapping("/system")
    public String system(@RequestParam("message") String userInput) {
        String systemPrompt = "你是一个专业的Java开发人员，请根据用户输入给出合理的建议。";

        ChatResponse response = chatClient.prompt()
                .system(systemPrompt)
                .user(userInput)
                .call()
                .chatResponse();

        return response.getResult().getOutput().getText();

    }

    @PostMapping("/template")
    public String template(@RequestParam("message") String userInput,
                                 @RequestParam(value = "name", required = false) String aiName) {
        PromptTemplate template = new PromptTemplate("你的名字是{name},请根据用户输入给出合理的建议。");
        Prompt prompt = template.create(Map.of("name", aiName == null ? "KiKoRepub" : aiName));

        String response = chatClient.prompt(prompt)
                .user(userInput)
                .call()
                .content();

        return response;
    }

    // 处理/systemTemplate路径的POST请求
    @PostMapping("/systemTemplate")
    public String systemTemplate(@RequestParam("message") String userInput,
                                 @RequestParam(value = "name", required = false) String aiName,
                                 @RequestParam(value = "language",required = false) String language) {
        // 定义模板字符串
        String templateStr = """
                你的名字是{name},只会回答关于{language}编程语言的问题。
                """;
        // 创建SystemPromptTemplate对象
        SystemPromptTemplate systemTemplate = new SystemPromptTemplate(templateStr);
        // 创建systemPrompt对象
        Message systemPrompt = systemTemplate.createMessage(
                Map.of("name", aiName == null ? "KiKoRepub" : aiName,
                        "language", language == null ? "Java" : language));

        // 创建userPrompt对象
        Message userPrompt = new UserMessage("Stavy 语言如何？");
        // 创建assertPrompt对象
        Message assertPrompt = new AssistantMessage("对不起，我只会回答关于" + language + "语言的内容，请询问我" + language + "相关的问题。");

        // 创建Prompt对象
        Prompt prompt = new Prompt(List.of(systemPrompt, userPrompt, assertPrompt));

        // 调用chatClient的prompt方法，传入prompt对象，并设置userInput
        String result = chatClient.prompt(prompt)
                .user(userInput)
                .call()
                .content();

        // 返回结果
        return result;
    }
    // 通过 value 加载文件 内容，转换成对象
    @Value("classpath: /prompts/systemTestPrompt.txt")
    private Resource systemTestPrompt;

    @PostMapping("/systemTemplate/file")
    public String systemTemplateFile(@RequestParam("message") String userInput,
                                 @RequestParam(value = "name", required = false) String aiName,
                                 @RequestParam(value = "language",required = false) String language) {
        // 定义模板字符串
//        systemTestPrompt = new UrlResource("http://localhost:8080/prompts/systemTestPrompt.txt");

        SystemPromptTemplate systemTemplate = new SystemPromptTemplate(systemTestPrompt);
        Message systemPrompt = systemTemplate.createMessage(Map.of("name", aiName == null ? "KiKoRepub" : aiName,
                "language", language == null ? "Java" : language));
        Prompt prompt = new Prompt(List.of(systemPrompt));
        String result = chatClient.prompt(prompt)
                .user(userInput)
                .call()
                .content();

        return result;

    }




}
