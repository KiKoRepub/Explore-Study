package org.dee.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.dee.enums.PersistenceType;

import java.time.LocalDateTime;

@Data
@TableName("chat_record")
public class ChatRecord {

    @TableId
    @ApiModelProperty("ID")
    private Integer id;

    @TableField("user_id")
    @ApiModelProperty("User ID")
    private String userId;

    @TableField("conversation_id")
    @ApiModelProperty("Conversation ID")
    private String conversationId;

    @TableField("user_message")
    @ApiModelProperty("User Message")
    private String userMessage;

    @TableField("bot_response")
    @ApiModelProperty("Bot Response")
    private String botResponse;

    @TableField("created_at")
    @ApiModelProperty("Creation Timestamp")
    private LocalDateTime createdAt;

    @TableField("persistence_type_code")
    @ApiModelProperty("Persistence Type: auto-自动持久化, manual-手动持久化")
    private String persistenceTypeCode;

    @TableField("persistence_time")
    @ApiModelProperty("Persistence Time")
    private LocalDateTime persistenceTime;

}
