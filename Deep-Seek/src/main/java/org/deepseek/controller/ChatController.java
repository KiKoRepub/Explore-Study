package org.deepseek.controller;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.deepseek.DeepSeekAssistantMessage;
import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.ai.deepseek.DeepSeekChatOptions;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/chat")
public class ChatController extends AIController {

    @Autowired
    ChatMemoryRepository memoryRepository;

    @PostMapping("/push")
    public String push(@RequestParam("message") String message) {

        ChatResponse response = chatClient.prompt(message)
                .call().chatResponse();
//        ChatClient.ChatClientRequestSpec request = chatClient
//                .prompt(PromptUtils.getCodeAssistantPrompt())
//                .user(message)
//                .tools(new GetWeatherTool());
//
//        System.out.println(request);


        return response.getResult().getOutput().getText();
    }

    @PostMapping("/stream")
    public Flux<Object> streamChat(HttpServletResponse response) throws IOException {
        response.setCharacterEncoding("UTF-8");
        String message = DEFAULT_PROMPT;
        ObjectMapper mapper = new ObjectMapper();
        if (message.equals(DEFAULT_PROMPT)) {
            FileReader reader = new FileReader("D:\\university\\JAVA\\Explore-Study\\Deep-Seek\\src\\main\\resources\\refer-thinking.json");
            StringBuilder result = new StringBuilder();
            char[] bytes = new char[1024];
            while ((reader.read(bytes)) != -1) {
                result.append(Arrays.toString(bytes));
            }
            List responses = mapper.readValue(reader, List.class);
            List<ChatResponse> chatResponses = new ArrayList<>();
            for (Object res : responses) {
                try {
                    ChatResponse chatResponse = JSON.parseObject(res.toString(), ChatResponse.class);
                    chatResponses.add(chatResponse);
                } catch (Exception e) {
                    if (res instanceof ChatResponse chatResponse) {
                        chatResponses.add(chatResponse);
                    }
                }
            }
//      Flux<ChatResponse>
            return Flux.fromIterable(chatResponses);


        }
        Flux<ChatResponse> stream = chatModel.stream(new Prompt(DEFAULT_PROMPT));

//      Flux<String>
        return stream.map(resp -> resp.getResult().getOutput().getText());
    }


    @GetMapping("/multi-push")
    public String deepPush(@RequestParam("message_list") List<String> messageList) {
        ChatResponse request = chatClient.prompt()
                .messages(
                        messageList.stream()
                                .map(message -> UserMessage.builder()
                                        .text(message)
                                        .build())
                                .toArray(UserMessage[]::new)
                ).call()
                .chatResponse();

        return request.getResult().getOutput().getText();

    }


    @PostMapping("/reasoner-push")
    public String resonerPush(@RequestParam("message")String message ) {
        if (chatModel instanceof DeepSeekChatModel) {

            Prompt prompt = new Prompt(message, DeepSeekChatOptions.builder()
                    .model("deepseek-reasoner")
                    .temperature(1.5)
                    .build());

            ChatResponse response = chatModel.call(prompt);


            DeepSeekAssistantMessage output = (DeepSeekAssistantMessage) response.getResult().getOutput();

            String resoningContent = output.getReasoningContent();
            String result = output.getText();

            System.out.println("result = " + result);
            System.out.println("——————————————————————————————————————");
            System.out.println("resoningContent = " + resoningContent);

            return result;
        }else return "当前模型不支持 深度思考功能";
    }

    @PostMapping("/stream/reasoner-push")
    public Flux<Object> reasonerStreamPush(@RequestParam("message") String message) {
        Prompt prompt = new Prompt(message, DeepSeekChatOptions.builder()
                .model("deepseek-reasoner")
                .temperature(1.5)
                .build());

        Flux<ChatResponse> response = chatModel.stream(prompt);



        response.toIterable().forEach(resp -> {

            DeepSeekAssistantMessage output = (DeepSeekAssistantMessage) resp.getResult().getOutput();

            String reasoningContent = output.getReasoningContent();

            if (reasoningContent != null) {
                System.out.print(reasoningContent + " ");
            }
        });

        response.toIterable().forEach(resp -> {
            DeepSeekAssistantMessage output = (DeepSeekAssistantMessage) resp.getResult().getOutput();
            String result = output.getText();
            System.out.print(result + " ");
        });

        return response.map(resp -> resp.getResult().getOutput().getText());

    }


    @PostMapping("/stream/logging-push")
    public Flux<String> loggingStreamPush(@RequestParam("message") String message) {

        Flux<String> flux = chatClient.prompt()
                .advisors(List.of(SimpleLoggerAdvisor.builder().build()))
                .user(message)
                .stream()
                .content();

        return flux.map(resp -> resp);
    }






}
