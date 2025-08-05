package org.deepseek.tools;


import org.deepseek.entity.ChatMessage;
import org.deepseek.service.ChatMessageService;
import org.deepseek.utils.RedisUtils;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Description("用来管理聊天历史的工具类")
public class ChatHistoryManageTool {

    @Autowired
    ChatMessageService cpService;

    @Tool(description = "将redis中的聊天记录保存到mysql中")
    public String saveRedisToMySQL(@ToolParam(description = "当前对话的id",required = true) String conversationId){

        List<ChatMessage> cacheList =
                RedisUtils.getCacheAIRecordList(conversationId);

        boolean saveResult = cpService.saveRecords(cacheList);
        if (saveResult){
            RedisUtils.setExpireAIRecord(conversationId,60 * 5);
            return "保存成功";
        }

        return  "保存失败";
    }

    @Tool(description = "读取之前保存到数据库中的聊天记录")
    public String readMySQLRecord(@ToolParam(description = "之前的对话id",required = true) String conversationId){
        List<ChatMessage> cacheList =
                cpService.readRecords(conversationId);

        boolean b = RedisUtils.pushSQLCacheRecordList(conversationId, cacheList);


        return b ? "读取成功": "读取失败";
    }


}
