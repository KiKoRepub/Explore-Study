-- 创建工具表
CREATE TABLE IF NOT EXISTS `tool` (
    `id` INT AUTO_INCREMENT PRIMARY KEY COMMENT '工具ID',
    `tool_name` VARCHAR(100) NOT NULL COMMENT '工具名称',
    `description` VARCHAR(500) COMMENT '工具描述',
    `class_name` VARCHAR(255) NOT NULL COMMENT '工具类的完整类名',
    `method_name` VARCHAR(100) NOT NULL COMMENT '工具方法名',
    `parameters` TEXT COMMENT '工具参数定义',
    `enabled` TINYINT DEFAULT 1 COMMENT '是否启用: 1-启用, 0-禁用',
    `category` VARCHAR(50) COMMENT '工具分类',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_enabled` (`enabled`),
    INDEX `idx_category` (`category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工具表';
