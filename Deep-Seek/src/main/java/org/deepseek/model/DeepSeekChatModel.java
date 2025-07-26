package org.deepseek.model;

import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import reactor.core.publisher.Flux;

public class DeepSeekChatModel implements ChatModel {
    @Override
    public String call(String message) {
        return ChatModel.super.call(message);
    }

    @Override
    public String call(Message... messages) {
        return ChatModel.super.call(messages);
    }

    @Override
    public ChatResponse call(Prompt prompt) {
        return null;
    }

    @Override
    public ChatOptions getDefaultOptions() {
        return ChatModel.super.getDefaultOptions();
    }

    @Override
    public Flux<ChatResponse> stream(Prompt prompt) {
        return ChatModel.super.stream(prompt);
    }
}
