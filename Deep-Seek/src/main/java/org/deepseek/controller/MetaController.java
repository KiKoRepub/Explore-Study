package org.deepseek.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
@Deprecated
@RestController
@RequestMapping("/meta")
public class MetaController extends AIController {
    public MetaController(OpenAiChatModel openAiChatModel) {
        super(openAiChatModel);
    }

    @GetMapping("/push")
    public String pushOther(@RequestParam("message") String message) {
        ChatModel model = null;

        return ChatClient.create(model)
                .prompt()
                .user(u ->
                        u.text("帮我解析这张图片" + message)
                                .media(MediaType.IMAGE_PNG, new ClassPathResource("/images/1.png"))
                )
                .call()
                .content();
    }


}
