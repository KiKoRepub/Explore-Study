package org.dee.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@TableName("chat_record_zip")
public class ChatRecordZip {

    @TableId
    @ApiModelProperty("ID")
    private Integer id;

    @ApiModelProperty("Conversation ID")
    private String conversationId;

    @ApiModelProperty("Title")
    private String title;

    @ApiModelProperty("Compressed Data")
    private String compressedData;

}
