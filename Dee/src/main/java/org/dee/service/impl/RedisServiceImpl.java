package org.dee.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.dee.entity.dto.RedisChatMessageDTO;
import org.dee.entity.ChatRecord;
import org.dee.service.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Redis 服务实现类
 * 使用 RedisTemplate 实现线程安全的 Redis 操作
 * 
 * 优势：
 * 1. 线程安全：RedisTemplate 内部使用连接池
 * 2. 自动序列化/反序列化
 * 3. 统一异常处理
 * 4. 支持事务
 */
@Slf4j
@Service
@ConditionalOnBean(RedisCacheChatService.class)
public class RedisServiceImpl implements RedisService {

    /**
     * 聊天记录键前缀
     */
    private static final String SPRING_AI_RECORD_PREFIX = "spring_ai_alibaba_chat_memory:";

    /**
     * 过期标记键前缀
     */
    private static final String CHAT_EXPIRE_KEY_PREFIX = "chat:expire:";

    /**
     * 默认过期时间（30分钟）
     */
    private static final Integer DEFAULT_RECORD_TTL = 60 * 30;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public <T> List<T> getCacheList(String cacheKey, Class<T> clazz) {
        try {
            List<String> jsonList = stringRedisTemplate.opsForList().range(cacheKey, 0, -1);
            if (jsonList == null || jsonList.isEmpty()) {
                return new ArrayList<>();
            }

            return jsonList.stream()
                    .map(json -> JSON.parseObject(json, clazz))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("获取缓存列表失败: cacheKey={}", cacheKey, e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<RedisChatMessageDTO> getCacheAIRecordList(String conversationId) {
        try {
            String cacheKey = SPRING_AI_RECORD_PREFIX + conversationId;
            List<String> jsonList = stringRedisTemplate.opsForList().range(cacheKey, 0, -1);
            
            if (jsonList == null || jsonList.isEmpty()) {
                log.debug("未找到缓存记录: conversationId={}", conversationId);
                return new ArrayList<>();
            }

            List<RedisChatMessageDTO> result = jsonList.stream()
                    .map(json -> JSON.parseObject(json, RedisChatMessageDTO.class))
                    .collect(Collectors.toList());

            log.debug("获取缓存记录成功: conversationId={}, 数量={}", conversationId, result.size());
            return result;
        } catch (Exception e) {
            log.error("获取 AI 聊天记录缓存失败: conversationId={}", conversationId, e);
            return new ArrayList<>();
        }
    }

    @Override
    public boolean setExpireAIRecord(String conversationId, long expireSeconds) {
        try {
            String cacheKey = SPRING_AI_RECORD_PREFIX + conversationId;
            Boolean success = stringRedisTemplate.expire(cacheKey, expireSeconds, TimeUnit.SECONDS);
            
            if (Boolean.TRUE.equals(success)) {
                log.debug("设置缓存过期时间成功: conversationId={}, 过期时间={}秒", conversationId, expireSeconds);
                return true;
            } else {
                log.warn("设置缓存过期时间失败: conversationId={}", conversationId);
                return false;
            }
        } catch (Exception e) {
            log.error("设置缓存过期时间异常: conversationId={}", conversationId, e);
            return false;
        }
    }

    @Override
    public boolean setExpireMarker(String conversationId, long expireSeconds) {
        try {
            String expireKey = CHAT_EXPIRE_KEY_PREFIX + conversationId;
            // 设置标记值为 conversationId，并设置过期时间
            stringRedisTemplate.opsForValue().set(expireKey, conversationId, expireSeconds, TimeUnit.SECONDS);
            
            log.info("✓ 设置过期标记: conversationId={}, 过期时间={}秒", conversationId, expireSeconds);
            return true;
        } catch (Exception e) {
            log.error("❌ 设置过期标记失败: conversationId={}", conversationId, e);
            return false;
        }
    }

    @Override
    public boolean removeExpireMarker(String conversationId) {
        try {
            String expireKey = CHAT_EXPIRE_KEY_PREFIX + conversationId;
            Boolean success = stringRedisTemplate.delete(expireKey);
            
            if (Boolean.TRUE.equals(success)) {
                log.debug("删除过期标记成功: conversationId={}", conversationId);
                return true;
            } else {
                log.debug("过期标记不存在或已删除: conversationId={}", conversationId);
                return false;
            }
        } catch (Exception e) {
            log.error("删除过期标记失败: conversationId={}", conversationId, e);
            return false;
        }
    }

    @Override
    public boolean removeAIRecordCache(String conversationId) {
        try {
            String cacheKey = SPRING_AI_RECORD_PREFIX + conversationId;
            Boolean success = stringRedisTemplate.delete(cacheKey);
            
            if (Boolean.TRUE.equals(success)) {
                log.debug("删除缓存成功: conversationId={}", conversationId);
                return true;
            } else {
                log.warn("缓存不存在或已删除: conversationId={}", conversationId);
                return false;
            }
        } catch (Exception e) {
            log.error("删除缓存失败: conversationId={}", conversationId, e);
            return false;
        }
    }

    @Override
    public boolean pushSQLCacheRecordList(String conversationId, List<ChatRecord> recordList,long expireSeconds) {
        try {
            if (recordList == null || recordList.isEmpty()) {
                log.warn("聊天记录列表为空: conversationId={}", conversationId);
                return false;
            }

            String cacheKey = SPRING_AI_RECORD_PREFIX + conversationId;

            // 批量推送到 Redis List
            List<String> jsonList = recordList.stream()
                    .map(JSON::toJSONString)
                    .collect(Collectors.toList());

            stringRedisTemplate.opsForList().rightPushAll(cacheKey, jsonList);

            // 设置过期时间
            expireSeconds = expireSeconds <= 0 ? DEFAULT_RECORD_TTL : expireSeconds;
            setExpireAIRecord(conversationId, expireSeconds);

            log.info("保存数据库记录到缓存成功: conversationId={}, 数量={}", conversationId, recordList.size());
            return true;
        } catch (Exception e) {
            log.error("保存数据库记录到缓存失败: conversationId={}", conversationId, e);
            return false;
        }
    }

    @Override
    public boolean pushCacheRecordList(String conversationId, List<RedisChatMessageDTO> recordList, long expireSeconds){
        try {
            if (recordList == null || recordList.isEmpty()) {
                log.warn("聊天记录列表为空: conversationId={}", conversationId);
                return false;
            }

            String cacheKey = SPRING_AI_RECORD_PREFIX + conversationId;

            // 批量推送到 Redis List
            List<String> jsonList = recordList.stream()
                    .map(JSONObject::toJSONString)
                    .collect(Collectors.toList());

            stringRedisTemplate.opsForList().rightPushAll(cacheKey, jsonList);

            // 设置过期时间
                expireSeconds = expireSeconds <= 0 ? DEFAULT_RECORD_TTL : expireSeconds;

                setExpireAIRecord(conversationId, expireSeconds);

            log.debug("保存聊天记录到缓存成功: conversationId={}, 数量={}", conversationId, recordList.size());
            return true;
        } catch (Exception e) {
            log.error("保存聊天记录到缓存失败: conversationId={}", conversationId, e);
            return false;
        }
    }
}
