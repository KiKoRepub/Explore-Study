package org.grpc.redis;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.data.redis.core.RedisTemplate;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.List;

public class RedisUtils {


    private static final Jedis jedisClient;



    private static final String HISTORY_KEY = "chat:history";
    private static final long HISTORY_MIN_TTL = 30L * 60;
    private static final String HISTORY_MIDDLE_KEY = "|+|";


    public static boolean setHistoryRecord(String userName,String targetUser,String message) {
//        long recordLen = jedisClient.llen(userName);
//        List<String> history  = jedisClient.lrange(userName, 0, recordLen);
        // 信息发起方
        String cacheKey = HISTORY_KEY + userName + HISTORY_MIDDLE_KEY + targetUser;
        try {
//            List<String> history = data == null ? null : JSON.parseArray(data, String.class);
//            if (history == null) {
//                history = new ArrayList<>();
//            }
//            history.add(message);
//            String hasSet = jedisClient.set(userName, history.toString());
            long hasSet = jedisClient.lpush(cacheKey,message);
            jedisClient.expire(cacheKey, HISTORY_MIN_TTL);

          return hasSet == 1;

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("转换失败");
            return false;
        }
    }

    public static List<String> getHistoryRecord(String userName,String targetUser) {
        // 接收信息
        String cacheKey = HISTORY_KEY + targetUser + HISTORY_MIDDLE_KEY + userName;
        try {
            if (jedisClient.exists(cacheKey)) {
//                long recordLen = jedisClient.llen(userName);
//                System.out.println(recordLen);
                return jedisClient.lrange(cacheKey, 0, -1);
//            return mapper.readValue(jedisClient.get(userName), List.class);
            }else return new ArrayList<>();

        }catch(Exception e){
            e.printStackTrace();
            throw new RuntimeException("data parse error");
        }

    }


    static {
        jedisClient = new Jedis("localhost", 6379);
    }


    public static void main(String[] args) {
//        System.out.println(jedisClient.set("666", "666"));
//        jedisClient.flushDB();
        System.out.println(setHistoryRecord("lcy","hutao","赤团开时斜飞去"));
        System.out.println(setHistoryRecord("lcy","hutao","最不安神晴又复雨"));
        System.out.println(setHistoryRecord("lcy","hutao","逗留采血色"));
        System.out.println(setHistoryRecord("lcy","hutao","伴君眠花房"));
        System.out.println(setHistoryRecord("lcy","hutao","无可奈何燃花作香"));
        System.out.println(setHistoryRecord("lcy","hutao","幽蝶能留一缕芳"));
        System.out.println(getHistoryRecord("hutao","lcy"));
//        System.out.println(jedisClient.get("666"));
    }

}
