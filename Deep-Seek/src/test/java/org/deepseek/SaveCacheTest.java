package org.deepseek;

import org.deepseek.entity.ChatMessage;
import org.deepseek.service.ChatMessageService;
import org.deepseek.utils.RedisUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class SaveCacheTest {

    @Autowired
    ChatMessageService cachePersistenceService;
    @Test
    public void test() {

        List<ChatMessage> cacheAIRecordList = RedisUtils.getCacheAIRecordList("12");


        cachePersistenceService.saveRecords(cacheAIRecordList);

    }

    @Test
    public void test2(){
        List<ChatMessage> cacheList =
                cachePersistenceService.readRecords("12");
        System.out.println(cacheList);
        boolean b = RedisUtils.pushSQLCacheRecordList("12", cacheList);
    }

}
