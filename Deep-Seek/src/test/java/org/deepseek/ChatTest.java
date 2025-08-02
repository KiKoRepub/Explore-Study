package org.deepseek;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.deepseek.DeepSeekAssistantMessage;
import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.ai.deepseek.DeepSeekChatOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ChatTest {



    @Test
    public void testReasonerOutput(@Autowired DeepSeekChatModel deepSeekChatModel) {

        Prompt prompt = new Prompt("你好，我是谁？", DeepSeekChatOptions.builder()
                .model("deepseek-reasoner")
                .temperature(1.5)
                .build());

        ChatResponse response = deepSeekChatModel.call(prompt);


        DeepSeekAssistantMessage output = (DeepSeekAssistantMessage) response.getResult().getOutput();

        String resoningContent = output.getReasoningContent();
        String result = output.getText();

        System.out.println("result = " + result);
        System.out.println("——————————————————————————————————————");
        System.out.println("resoningContent = " + resoningContent);

    }

}
