package org.example;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/chat")
public class ChatController {
    @Autowired
    protected ChatClient chatClient;

//    public ChatController(OpenAiChatModel openAiChatModel) {
//        super(openAiChatModel);
//    }

    @GetMapping("/push")
    public String push(@RequestParam("message") String message) {

        ChatResponse response = chatClient.prompt(message)
                .call().chatResponse();

        return response.getResult().getOutput().getText();
    }

}
