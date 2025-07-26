package org.deepseek.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.parser.Feature;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/chat")
public class ChatController extends AIController {
    @Autowired
    protected ChatClient chatClient;

    public ChatController(OpenAiChatModel openAiChatModel) {
        super(openAiChatModel);
    }

    @GetMapping("/push")
    public String push(@RequestParam("message") String message) {

        ChatResponse response = chatClient.prompt(message)
                .call().chatResponse();

        return response.getResult().getOutput().getText();
    }

    @GetMapping("/stream")
    public Flux<Object> streamChat (HttpServletResponse response) throws IOException {
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
            for (Object res : responses){
                try{
                    ChatResponse chatResponse = JSON.parseObject(res.toString(), ChatResponse.class);
                    chatResponses.add(chatResponse);
                }catch (Exception e){
                    if (res instanceof ChatResponse chatResponse){
                        chatResponses.add(chatResponse);
                    }
                }
            }
//      Flux<ChatResponse>
            return Flux.fromIterable(chatResponses);


        }
        Flux<ChatResponse> stream = deepSeekChatModel.stream(new Prompt(DEFAULT_PROMPT));

//      Flux<String>
        return stream.map(resp -> resp.getResult().getOutput().getText());
    }


    @GetMapping("/multi-push")
    public String deepPush(@RequestParam("message_list") List<String> messageList) {
        for (String message : messageList) {
            ChatResponse response = chatClient.prompt(message).call().chatResponse();

        }


        return "";
    }




}
