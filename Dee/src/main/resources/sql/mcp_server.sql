-- MCP服务器表
CREATE TABLE IF NOT EXISTS `sql_mcp_server` (
    `id` INT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    `server_name` VARCHAR(100) NOT NULL COMMENT 'MCP服务器名称',
    `server_url` VARCHAR(500) COMMENT 'MCP服务器URL',
    `description` VARCHAR(500) COMMENT '服务器描述',
    `endpoint` VARCHAR(200) COMMENT 'SSE/HTTP端点路径',
    `type` VARCHAR(20) NOT NULL COMMENT '服务器类型：STDIO, HTTP, SSE',
    `json_content` TEXT COMMENT 'STDIO类型的JSON配置内容',
    `enabled` TINYINT(1) DEFAULT 1 COMMENT '是否启用：0-禁用，1-启用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_server_name` (`server_name`),
    INDEX `idx_type` (`type`),
    INDEX `idx_enabled` (`enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MCP服务器配置表';

-- 插入示例数据
INSERT INTO `sql_mcp_server` (`server_name`, `server_url`, `description`, `endpoint`, `type`, `json_content`, `enabled`) VALUES
('高德地图MCP服务', 'https://mcp.amap.com', '高德地图MCP服务，提供地图相关功能', '/sse?key=${AMAP_API_KEY}', 'SSE', NULL, 1),
('本地书籍MCP服务', 'http://localhost:8085', '本地书籍管理MCP服务', '/mcp/book', 'SSE', NULL, 1),
('Chrome开发工具', NULL, 'Chrome DevTools MCP服务', NULL, 'STDIO', '{"command":"npx.cmd","args":["chrome-devtools-mcp@latest"],"env":{}}', 0);
