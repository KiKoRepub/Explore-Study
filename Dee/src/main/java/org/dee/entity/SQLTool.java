package org.dee.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sql_tool")
public class SQLTool {

    @TableId(type = IdType.AUTO)
    @ApiModelProperty("工具ID")
    private Integer id;

    @TableField("tool_name")
    @ApiModelProperty("工具名称")
    private String toolName;

    @TableField("description")
    @ApiModelProperty("工具描述")
    private String description;

    @TableField("class_name")
    @ApiModelProperty("工具类的完整类名(MCP对应 ServerName,USER 对应 用户id )")
    private String className;


    @TableField("input_schema")
    @ApiModelProperty("工具参数定义(JSON格式)")
    private String inputSchema;

    @TableField("enabled")
    @ApiModelProperty("是否启用: 1-启用, 0-禁用")
    private Integer enabled;

    @TableField("category")
    @ApiModelProperty("工具分类")
    private String category;

    @TableField("created_at")
    @ApiModelProperty("创建时间")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    @ApiModelProperty("更新时间")
    private LocalDateTime updatedAt;
}
