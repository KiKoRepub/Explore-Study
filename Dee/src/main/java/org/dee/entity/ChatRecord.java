package org.dee.entity;

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
    @ApiModelProperty("Conversation ID")
    private String conversationId;

    @ApiModelProperty("User Message")
    private String userMessage;

    @ApiModelProperty("Bot Response")
    private String botResponse;

    @ApiModelProperty("Creation Timestamp")
    private LocalDateTime createdAt;

    @ApiModelProperty("Persistence Type: auto-自动持久化, manual-手动持久化")
    private String persistenceTypeCode;
    @ApiModelProperty("Persistence Time")
    private LocalDateTime persistenceTime;

}
