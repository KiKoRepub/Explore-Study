package org.deepseek.advisor;

import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.chat.prompt.PromptTemplate;

import java.util.Map;
/*
通过自定义 Advisor 对 用户输入 进行干预
 */
public class ReReadAdvisor implements BaseAdvisor {

    private static final String DEFAULT_USER_TEXT_ADVISE = """
      {re2_input_query}
      Read the question again: {re2_input_query}
      """;

    @Override
    public ChatClientRequest before(ChatClientRequest chatClientRequest, AdvisorChain advisorChain) {

        // 获取用户的输入 (在请求执行前 对信息进行增强 )
        // 这个 Advisor 是 让AI 重复参考输入内容 总共 两次
        String userMessage = chatClientRequest.prompt().getUserMessage().getText();
        chatClientRequest.context().put("lastBefore",getName());

        String augmentedSystemText = PromptTemplate.builder().template(DEFAULT_USER_TEXT_ADVISE).build()
                .render(Map.of("re2_input_query", userMessage));

        ChatClientRequest request = chatClientRequest.mutate()
                .prompt(chatClientRequest.prompt().augmentUserMessage(augmentedSystemText))
                .build();



        return request;
    }

    @Override
    public ChatClientResponse after(ChatClientResponse chatClientResponse, AdvisorChain advisorChain) {
        // 请求结束后，尝试输出 执行日志，表示经过了这个 Advisor
        System.out.println("ReReadAdvisor.after");
        return chatClientResponse;
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
