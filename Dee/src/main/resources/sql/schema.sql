-- 创建聊天记录表
CREATE TABLE IF NOT EXISTS chat_record (
    id INT AUTO_INCREMENT PRIMARY KEY COMMENT 'ID',
    conversation_id VARCHAR(255) NOT NULL COMMENT '对话ID',
    user_message TEXT NOT NULL COMMENT '用户消息',
    bot_response TEXT NOT NULL COMMENT '机器人回复',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_conversation_id (conversation_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='聊天记录表';

-- 创建对话概要表
CREATE TABLE IF NOT EXISTS chat_record_zip (
    id INT AUTO_INCREMENT PRIMARY KEY COMMENT 'ID',
    conversation_id VARCHAR(255) NOT NULL UNIQUE COMMENT '对话ID',
    title VARCHAR(500) COMMENT '标题',
    compressed_data TEXT COMMENT '压缩的概要数据',
    INDEX idx_conversation_id (conversation_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='对话概要表';
