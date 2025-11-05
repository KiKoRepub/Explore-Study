-- 创建工具审计日志表
CREATE TABLE IF NOT EXISTS `tool_audit_log` (
    `id` INT AUTO_INCREMENT PRIMARY KEY COMMENT '日志ID',
    `conversation_id` VARCHAR(255) NOT NULL COMMENT '对话ID',
    `request_id` VARCHAR(255) NOT NULL COMMENT '请求ID',
    `tool_name` VARCHAR(255) NOT NULL COMMENT '工具名称',
    `method_name` VARCHAR(255) NOT NULL COMMENT '方法名称',
    `status` VARCHAR(50) NOT NULL COMMENT '执行状态: STARTED, COMPLETED, FAILED',
    `parameters` TEXT COMMENT '工具参数(JSON格式)',
    `result` TEXT COMMENT '执行结果(JSON格式)',
    `error_message` TEXT COMMENT '错误信息',
    `execution_time` BIGINT COMMENT '执行耗时(毫秒)',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `start_at` DATETIME COMMENT '开始时间',
    `end_at` DATETIME COMMENT '结束时间',
    `user_id` VARCHAR(255) COMMENT '用户ID',
    `ip_address` VARCHAR(50) COMMENT 'IP地址',
    INDEX `idx_conversation_id` (`conversation_id`),
    INDEX `idx_tool_name` (`tool_name`),
    INDEX `idx_status` (`status`),
    INDEX `idx_created_at` (`created_at`),
    INDEX `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工具审计日志表';
