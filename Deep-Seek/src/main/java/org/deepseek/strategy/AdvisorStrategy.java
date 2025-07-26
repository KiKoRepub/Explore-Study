package org.deepseek.strategy;

import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;

public class AdvisorStrategy {

    public static final Advisor MESSAGE_CHAT_MEMORY_ADVISOR = MessageChatMemoryAdvisor.builder(
            MessageWindowChatMemory.builder().build()
    ).build();
    public static final Advisor LOGGING_ADVISOR = new SimpleLoggerAdvisor();
}
