package org.dee.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.dee.enums.McpServerTypeEnum;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class McpServerVo {

    /**
     * 服务器ID
     */
    private Integer id;
    
    /**
     * MCP服务器名称
     */
    private String serverName;
    
    /**
     * MCP服务器URL
     */
    private String serverUrl;
    
    /**
     * 服务器描述
     */
    private String description;

    /**
     * SSE/HTTP端点路径
     */
    private String endpoint;

    /**
     * 服务器类型：STDIO, HTTP, SSE
     */
    private McpServerTypeEnum type;
    
    /**
     * STDIO类型的JSON配置内容
     */
    private String jsonContent;
    
    /**
     * 是否启用
     */
    private Boolean enabled;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    
    /**
     * 连接状态（运行时状态，非数据库字段）
     */
    private String connectionStatus;
    
    /**
     * 可用工具数量（运行时状态，非数据库字段）
     */
    private Integer toolCount;

}
