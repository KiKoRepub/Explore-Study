package org.deepseek.entity;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.deepseek.handler.JsonObjectTypeHandler;
import org.deepseek.handler.ListStringTypeHandler;

import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("chat_message")
public class ChatMessage {
   @TableId(type = IdType.AUTO)
   private Integer id;

   private String conversationId;
   private String messageType;

   @TableField(typeHandler = JsonObjectTypeHandler.class)
   private JSONObject metaData;

   @TableField(typeHandler = ListStringTypeHandler.class)
   private List<String> media; // 假设 media 是字符串列表
   @TableField(typeHandler = ListStringTypeHandler.class)
   private List<String> toolCalls;
   private String text;
   private LocalDateTime createTime;

}