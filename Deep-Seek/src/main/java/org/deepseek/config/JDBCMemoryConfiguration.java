package org.deepseek.config;

import com.alibaba.cloud.ai.memory.redis.RedisChatMemoryRepository;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.dynamic.datasource.provider.DynamicDataSourceProvider;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DataSourceProperty;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DynamicDataSourceProperties;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DynamicDatasourceAopProperties;
import com.mysql.cj.jdbc.MysqlDataSource;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.ai.chat.memory.repository.jdbc.MysqlChatMemoryRepositoryDialect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Configuration
public class JDBCMemoryConfiguration {


    @Autowired
    DynamicDataSourceProvider provider; // 动态数据源
    @Autowired
    RedisChatMemoryRepository redisChatMemoryRepository; // Redis 存储

    // 默认的 基于 ConcurrentHashMap 的内存存储
//    @Bean
//    public ChatMemory defaultChatMemory() {
//        return MessageWindowChatMemory.builder()
//                .chatMemoryRepository(new InMemoryChatMemoryRepository())
//                .maxMessages(20) // 最大消息数
//                .build();// 开启内存 信息存储功能
//    }

    // 添加的 基于 MySQL 的内存存储
//    @Bean
//    public ChatMemory mysqlChatMemory() {
//
//        JdbcTemplate template = new JdbcTemplate();
//
//
//
//        template.setDataSource(provider.loadDataSources().get("mysql"));
//
//
//        ChatMemoryRepository chatMemoryRepository = JdbcChatMemoryRepository.builder()
//                .jdbcTemplate(template)
//                .dialect(new MysqlChatMemoryRepositoryDialect())
//                .build();
//
//        return MessageWindowChatMemory.builder()
//                .chatMemoryRepository(chatMemoryRepository)
//                .maxMessages(1)
//                .build();
//    }

    // 添加的 基于 Redis 的内存存储
    @Bean
    public ChatMemory redisChatMemory() {
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(redisChatMemoryRepository)
                .maxMessages(20)
                .build();
    }

}
