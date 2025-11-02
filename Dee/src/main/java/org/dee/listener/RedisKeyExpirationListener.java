package org.dee.listener;


public class RedisKeyExpirationListener  {

}
/**
 * Redis 键过期事件监听器
 * 监听聊天记录的过期事件，触发批量持久化
 */
/*
@Slf4j
public class RedisKeyExpirationListener extends KeyExpirationEventMessageListener {


    private static final String CHAT_EXPIRE_KEY_PREFIX = "chat:expire:";

    @Autowired
    private CacheChatService cacheChatService;

    public RedisKeyExpirationListener(RedisMessageListenerContainer listenerContainer) {
        super(listenerContainer);
    }

    */
/**
     * 处理键过期事件
     *//*

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String expiredKey = message.toString();
        log.info("检测到 Redis 键过期: {}", expiredKey);

        // 只处理聊天过期键
        if (expiredKey.startsWith(CHAT_EXPIRE_KEY_PREFIX)) {
            String conversationId = expiredKey.substring(CHAT_EXPIRE_KEY_PREFIX.length());
            log.info("触发对话记录持久化: conversationId={}", conversationId);

            try {
                // 异步执行持久化操作
                cacheChatService.persistChatMessages(conversationId);
            } catch (Exception e) {
                log.error("持久化对话记录失败: conversationId={}", conversationId, e);
            }
        }
    }
}
*/
