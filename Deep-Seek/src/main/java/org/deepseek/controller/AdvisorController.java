package org.deepseek.controller;

import org.deepseek.strategy.AdvisorStrategy;
import org.jetbrains.annotations.NotNull;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/advisor")
public class AdvisorController extends AIController {

    public AdvisorController(OpenAiChatModel openAiChatModel) {
        super(openAiChatModel);
    }

    @GetMapping("/push")
    public String advisePush(@RequestParam("message") String message) {
        // 使用SpringAI 的 Advisor 机制
        //类似于 AOP 可以对 ChatClient 的方法进行拦截
        ChatModel model = null;

        // 将对话保存到 内存中 (开启内存记忆)
        ChatResponse response = chatClient
                .prompt()
                .user("yyy")
                .advisors(
                        getConcreteAdvisor(1)
                )
                .call()
                .chatResponse();




        return response.getResult().getOutput().getText();
    }


    @GetMapping("/memory/push")
    public String adviseMemoryPush(@RequestParam("message") String message,Integer chatId) {
        // 将对话保存到 内存中 (开启内存记忆) 并根据不同的 chatId 进行区分
        ChatResponse response = chatClient
                .prompt()
                .user(message)
                .advisors(getConcreteAdvisor(2))
                .advisors(spec -> spec.param("chat_memory_conversation_id_key",chatId)
                        .param("CHAT_MEMORY_RETRIEVE_SIZE_KEY",10))
                .call()
                .chatResponse();

        return response.getResult().getOutput().getText();
    }
    @NotNull
    private static Advisor getConcreteAdvisor(Integer number) {
        switch (number){
            case 1 -> {
                return AdvisorStrategy.MESSAGE_CHAT_MEMORY_ADVISOR;
            }
            default -> {
                return AdvisorStrategy.LOGGING_ADVISOR;
            }
        }

    }

}
