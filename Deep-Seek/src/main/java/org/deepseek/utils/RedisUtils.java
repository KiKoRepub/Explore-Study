package org.deepseek.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.deepseek.entity.ChatMessage;
import org.springframework.ai.chat.messages.Message;
import redis.clients.jedis.Jedis;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class RedisUtils {

    private static final Jedis jedisClient;


    private static final String SPRING_AI_RECORD_PREFIX = "spring_ai_alibaba_chat_memory:";

    private static final Integer DEFAULT_RECORD_TTL  = 60 * 30;

    static {
        jedisClient = new Jedis("127.0.0.1", 6379);
        jedisClient.auth("redis");
        LoggerUtils.info("——————————————本地Redis 连接成功—————————————————");
    }

    public static<T>  List<T>  getCacheList(String cacheKey,Class<T> clazz){
        try {
            List<T> result = new ArrayList<>();
//        String cacheKey = SPRING_AI_RECORD_PREFIX + key;
            List<String> list = jedisClient.lrange(cacheKey, 0, -1);
            for (String s : list) {
                T t = JSON.parseObject(s, clazz);
                result.add(t);
            }
            return result;
        }catch (Exception e){
            e.printStackTrace();
            LoggerUtils.error(e,"——————————————获取缓存失败—————————————————");
            return null;
        }
    }

    public static List<ChatMessage> getCacheAIRecordList(String conversationId){
        List<ChatMessage> result = new ArrayList<>();

        List<String> list = jedisClient.lrange(SPRING_AI_RECORD_PREFIX + conversationId, 0, -1);
        for (String cache : list) {


            ChatMessage cacheRecord = JSON.parseObject(cache, ChatMessage.class);
            cacheRecord.setConversationId(conversationId);

            if (cacheRecord.getToolCalls() == null) cacheRecord.setToolCalls(new ArrayList<>());

            result.add(cacheRecord);
        }

        return result;

    }


    public static boolean setExpireAIRecord(String conversationId, int expireSeconds){
        try {
            String cacheKey = SPRING_AI_RECORD_PREFIX + conversationId;
            long expire = jedisClient.expire(cacheKey, expireSeconds);
            return expire > 0;
        }catch (Exception e){
            e.printStackTrace();
            LoggerUtils.error(e,"——————————————设置缓存过期时间失败—————————————————");
            return false;
        }
    }


    public static boolean removeAIRecordCache(String key){
        try {
            String cacheKey = SPRING_AI_RECORD_PREFIX + key;
            long delete = jedisClient.del(cacheKey);
            return delete > 0;
        }catch (Exception e){
            e.printStackTrace();
            LoggerUtils.error(e,"——————————————删除缓存失败—————————————————");
            return false;
        }
    }


    public static boolean pushSQLCacheRecordList(String conversationId,List<ChatMessage> recordList){
        try{
            String cacheKey = SPRING_AI_RECORD_PREFIX + conversationId;
            for (ChatMessage record : recordList) {
                String json = JSON.toJSONString(record);
                jedisClient.rpush(cacheKey, json);
            }
            setExpireAIRecord(conversationId, DEFAULT_RECORD_TTL);
            return true;
        }catch (Exception e){
            LoggerUtils.error(e,"保存失败");
            jedisClient.close();
            return false;
        }
    }
    public static boolean pushCacheRecordList(String conversationId, List<Message> recordList){
        try {
            String cacheKey = SPRING_AI_RECORD_PREFIX + conversationId;
            for (Message message : recordList) {
                JSONObject json = (JSONObject) JSONObject.toJSON(message);
                json.put("create_time", LocalDateTime.now());
                String messageJson = JSON.toJSONString(json);
                jedisClient.rpush(cacheKey, messageJson);
            }
            setExpireAIRecord(conversationId, DEFAULT_RECORD_TTL);
            return true;
        }catch (Exception e){
            LoggerUtils.error(e,"保存失败");
            jedisClient.close();
            return false;
        }
    }




    public static void main(String[] args) {
        System.out.println(RedisUtils.getCacheAIRecordList("12"));
    }

}
