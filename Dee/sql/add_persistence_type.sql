-- 为 chat_record_zip 表添加持久化类型和持久化时间字段
-- 执行日期: 2025-11-02

-- 1. 添加持久化类型字段
ALTER TABLE chat_record_zip 
ADD COLUMN persistence_type VARCHAR(20) DEFAULT 'manual' COMMENT '持久化类型: auto-自动持久化, manual-手动持久化';

-- 2. 添加持久化时间字段
ALTER TABLE chat_record_zip 
ADD COLUMN persistence_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '持久化时间';

-- 3. 为已有数据设置默认值（可选）
UPDATE chat_record_zip 
SET persistence_type = 'manual', 
    persistence_time = NOW() 
WHERE persistence_type IS NULL;

-- 4. 添加索引（可选，用于查询优化）
CREATE INDEX idx_persistence_type ON chat_record_zip(persistence_type);
CREATE INDEX idx_persistence_time ON chat_record_zip(persistence_time);

-- 5. 查看表结构
DESC chat_record_zip;

-- 6. 验证数据
SELECT 
    id,
    conversation_id,
    title,
    persistence_type,
    persistence_time
FROM chat_record_zip
LIMIT 10;
