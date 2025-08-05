# CREATE TABLE IF NOT EXISTS SPRING_AI_CHAT_MEMORY (
#     `conversation_id` VARCHAR( 36)NOT NULL,
#     `content` TEXT NOT NULL,
#     `type` VARCHAR(10)NOT NULL,
#     `timestamp`TIMESTAMP NOT NULL,
#
#     INDEX `SPRING_AI_CHAT_MEMORY_CONVERSATION_ID_TIMESTAMP_IDX` (`conversation_id`,`timestamp`)
# );

CREATE TABLE IF NOT EXISTS chat_message (
                              id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
                              message_type VARCHAR(50) NOT NULL COMMENT '消息类型',
                              metadata_message_type VARCHAR(50) NOT NULL COMMENT '元数据中的消息类型',
                              `text` TEXT COMMENT '消息文本内容',
                              create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='聊天消息表';

-- 创建存储 media 的关联表（一对多关系）
CREATE TABLE IF NOT EXISTS chat_message_media (
                                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                    message_id BIGINT NOT NULL COMMENT '关联的消息ID',
                                    media_url VARCHAR(500) NOT NULL COMMENT '媒体资源URL',
                                    media_type VARCHAR(50) COMMENT '媒体类型',
                                    sort_order INT DEFAULT 0 COMMENT '排序顺序',
                                    FOREIGN KEY (message_id) REFERENCES chat_message(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;