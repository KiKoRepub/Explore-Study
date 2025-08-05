package org.deepseek.callback;

import com.alibaba.cloud.ai.memory.redis.RedisChatMemoryRepository;
import org.deepseek.utils.RedisUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.messages.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.List;

@Component
public class RedisChatMemoryRepositoryCallback implements ChatMemoryRepository, AutoCloseable {

    @Autowired
    private RedisChatMemoryRepository redisChatMemoryRepository;


    @Override
    public void close() throws Exception {
        redisChatMemoryRepository.close();
    }

    @NotNull
    @Override
    public List<String> findConversationIds() {
        return redisChatMemoryRepository.findConversationIds();
    }

    @NotNull
    @Override
    public List<Message> findByConversationId(@NotNull String conversationId) {
        return redisChatMemoryRepository.findByConversationId(conversationId);
    }

    @Override
    public void saveAll(@NotNull String conversationId, @NotNull List<Message> messages) {
        Assert.hasText(conversationId, "conversationId cannot be null or empty");
        Assert.notNull(messages, "messages cannot be null");
        Assert.noNullElements(messages, "messages cannot contain null elements");
        deleteByConversationId(conversationId);
        RedisUtils.pushCacheRecordList(conversationId, messages);

    }

    @Override
    public void deleteByConversationId(@NotNull String conversationId) {
        redisChatMemoryRepository.deleteByConversationId(conversationId);
    }
}
