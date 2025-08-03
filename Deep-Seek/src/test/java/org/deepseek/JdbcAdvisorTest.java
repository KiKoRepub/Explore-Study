package org.deepseek;

import com.baomidou.dynamic.datasource.provider.DynamicDataSourceProvider;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DataSourceProperty;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DynamicDataSourceProperties;
import com.mysql.cj.jdbc.MysqlDataSource;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.ai.chat.memory.repository.jdbc.MysqlChatMemoryRepositoryDialect;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@SpringBootTest
public class JdbcAdvisorTest {




    @Autowired
//    @Qualifier("ollamaChatClient")
    ChatClient chatClient;

    @Autowired
    @Qualifier("mysqlChatMemory")
    ChatMemory chatMemory;

    @Test
    public void testChatMemory(){
        String  message = "我们接下来进行角色扮演，请记住 我是 徐元直 /no_think";

        String conversationId = "1";

        ChatResponse response = chatClient.prompt()
                .advisors(MessageChatMemoryAdvisor.builder(chatMemory)
                        .conversationId(conversationId)
                        .build())
                .user(message)
                .call()
                .chatResponse();
        String text = response.getResult().getOutput().getText();

        System.out.println(text);

        message = " 你认识我吗，我是谁? /no_think";

        ChatResponse response2 = chatClient.prompt()
                .advisors(MessageChatMemoryAdvisor.builder(chatMemory)
                        .conversationId(conversationId)
                        .build())
                .user(message)
                .call()
                .chatResponse();
        String text2 = response2.getResult().getOutput().getText();
        System.out.println(text2);
    }
//    @Test
    public void testChatMemory2(){
        String  message = " 你认识我吗，我是谁? /no_think";

        String conversationId = "1";
        ChatResponse response = chatClient.prompt()
                .advisors(MessageChatMemoryAdvisor.builder(chatMemory)
                        .conversationId(conversationId)
                        .build())
                .user(message)
                .call()
                .chatResponse();

        String text = response.getResult().getOutput().getText();
        System.out.println(text);
    }

}
