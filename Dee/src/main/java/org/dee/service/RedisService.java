package org.dee.service;

import org.dee.entity.dto.RedisChatMessageDTO;
import org.dee.entity.ChatRecord;

import java.util.List;

/**
 * Redis 服务接口
 * 提供线程安全的 Redis 操作
 */
public interface RedisService {

    /**
     * 获取缓存列表
     * 
     * @param cacheKey 缓存键
     * @param clazz 目标类型
     * @return 缓存列表
     */
    <T> List<T> getCacheList(String cacheKey, Class<T> clazz);

    /**
     * 获取 AI 聊天记录缓存列表
     * 
     * @param conversationId 对话ID
     * @return 聊天记录列表
     */
    List<RedisChatMessageDTO> getCacheAIRecordList(String conversationId);

    /**
     * 设置 AI 聊天记录缓存的过期时间
     * 
     * @param conversationId 对话ID
     * @param expireSeconds 过期时间（秒）
     * @return 是否设置成功
     */
    boolean setExpireAIRecord(String conversationId, long expireSeconds);

    /**
     * 设置过期标记键，用于触发自动持久化
     * 
     * @param conversationId 对话ID
     * @param expireSeconds 过期时间（秒）
     * @return 是否设置成功
     */
    boolean setExpireMarker(String conversationId, long expireSeconds);

    /**
     * 删除过期标记键
     * 
     * @param conversationId 对话ID
     * @return 是否删除成功
     */
    boolean removeExpireMarker(String conversationId);

    /**
     * 删除 AI 聊天记录缓存
     * 
     * @param conversationId 对话ID
     * @return 是否删除成功
     */
    boolean removeAIRecordCache(String conversationId);

    /**
     * 将数据库中读取的聊天记录保存到 Redis 缓存
     * 
     * @param conversationId 对话ID
     * @param recordList 聊天记录列表
     * @return 是否保存成功
     */
    boolean pushSQLCacheRecordList(String conversationId, List<ChatRecord> recordList,long expireSeconds);
 
    /**
     * 将聊天记录列表保存到 Redis 缓存
     * 
     * @param conversationId 对话ID
     * @param recordList 聊天记录列表
     * @return 是否保存成功
     */
    boolean pushCacheRecordList(String conversationId, List<RedisChatMessageDTO> recordList,long expireSeconds);
}
