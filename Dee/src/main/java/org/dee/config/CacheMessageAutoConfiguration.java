package org.dee.config;

import org.dee.service.CacheChatService;
import org.dee.service.ChatRecordService;
import org.dee.service.ChatSummaryService;
import org.dee.service.impl.DefaultCacheChatServiceImpl;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CacheMessageAutoConfiguration {


    @Bean
    @ConditionalOnMissingBean(CacheChatService.class)
    public CacheChatService cacheChatService(ChatRecordService recordService, ChatSummaryService summaryService) {
        return new DefaultCacheChatServiceImpl(recordService,summaryService);
    }

}
