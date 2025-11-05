package org.dee.config;

import org.dee.service.CacheChatService;
import org.dee.service.ChatContextService;
import org.dee.service.impl.DefaultCacheChatServiceImpl;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CacheMessageAutoConfiguration {

    /**
     * 默认的缓存聊天服务实现
     * @param contextService 聊天上下文服务
     * @return  CacheChatService 实例
     */
    @Bean
    @ConditionalOnMissingBean(CacheChatService.class)
    public CacheChatService cacheChatService(ChatContextService contextService) {
        return new DefaultCacheChatServiceImpl(contextService);
    }

}
