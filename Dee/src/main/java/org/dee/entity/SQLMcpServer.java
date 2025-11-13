package org.dee.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.dee.enums.McpServerTypeEnum;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("sql_mcp_server")
public class SQLMcpServer {

    @TableId(type = IdType.AUTO)
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


    /*
    CREATE TABLE sql_mcp_server (
        id INT AUTO_INCREMENT PRIMARY KEY,
        server_name VARCHAR(255) NOT NULL,
        server_url VARCHAR(512) NOT NULL,
        description VARCHAR(1024),
        endpoint VARCHAR(255),
        type VARCHAR(50) NOT NULL,
        json_content TEXT,
        enabled BOOLEAN DEFAULT TRUE,
        create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
        update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
     */
}
