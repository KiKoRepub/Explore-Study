package org.deepseek.controller;

import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mcp")
public class MCPController extends AIController{

    @Autowired
    ToolCallbackProvider mcpToolProvider;

    public MCPController(OpenAiChatModel openAiChatModel) {
        super(openAiChatModel);
    }

    @GetMapping("/push")
    public String push(@RequestParam("message") String message) {
        ChatResponse response = chatClient.prompt()
                .user(message)
                .toolCallbacks(mcpToolProvider)
                .call()
                .chatResponse();

        String text = response.getResult().getOutput().getText();
        System.out.println("text = " + text);

        return text;

    }
}
