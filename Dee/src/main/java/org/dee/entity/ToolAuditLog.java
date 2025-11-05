package org.dee.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 工具审计日志实体类
 */
@Data
@TableName("tool_audit_log")
public class ToolAuditLog {

    @TableId(type = IdType.AUTO)
    @ApiModelProperty("日志ID")
    private Integer id;

    @TableField("conversation_id")
    @ApiModelProperty("对话ID")
    private String conversationId;

    @TableField("request_id")
    @ApiModelProperty("请求ID")
    private String requestId;

    @TableField("tool_name")
    @ApiModelProperty("工具名称")
    private String toolName;

    @TableField("method_name")
    @ApiModelProperty("方法名称")
    private String methodName;

    @TableField("status")
    @ApiModelProperty("执行状态: STARTED, COMPLETED, FAILED")
    private String status;

    @TableField("parameters")
    @ApiModelProperty("工具参数(JSON格式)")
    private String parameters;

    @TableField("result")
    @ApiModelProperty("执行结果(JSON格式)")
    private String result;

    @TableField("error_message")
    @ApiModelProperty("错误信息")
    private String errorMessage;

    @TableField("execution_time")
    @ApiModelProperty("执行耗时(毫秒)")
    private Long executionTime;

    @TableField("created_at")
    @ApiModelProperty("创建时间")
    private LocalDateTime createdAt;

    @TableField("start_at")
    @ApiModelProperty("开始时间")
    private LocalDateTime startAt;

    @TableField("end_at")
    @ApiModelProperty("结束时间")
    private LocalDateTime endAt;

    @TableField("user_id")
    @ApiModelProperty("用户ID")
    private String userId;

    @TableField("ip_address")
    @ApiModelProperty("IP地址")
    private String ipAddress;
}
