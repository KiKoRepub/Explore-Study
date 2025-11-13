package org.dee.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("chat_record_zip")
public class ChatRecordZip {

    @TableId
    @ApiModelProperty("ID")
    private Integer id;

    @TableField("user_id")
    @ApiModelProperty("User ID")
    private String userId;

    @TableField("conversation_id")
    @ApiModelProperty("Conversation ID")
    private String conversationId;

    @TableField("title")
    @ApiModelProperty("Title")
    private String title;

    @TableField("compressed_data")
    @ApiModelProperty("Compressed Data")
    private String compressedData;

    @TableField("created_at")
    @ApiModelProperty("Persistence Type: auto-自动持久化, manual-手动持久化")
    private String persistenceTypeCode;

    @TableField("persistence_time")
    @ApiModelProperty("Persistence Time")
    private LocalDateTime persistenceTime;

}
