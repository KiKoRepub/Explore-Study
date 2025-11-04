# 持久化类型区分使用指南

## 📋 概述

系统现在支持区分**自动持久化**和**手动持久化**，并在数据库中记录持久化类型和时间。

---

## 🎯 功能特性

### 1. 持久化类型枚举

```java
public enum PersistenceType {
    AUTO("auto", "自动持久化"),      // 通过 Redis 键过期事件触发
    MANUAL("manual", "手动持久化");  // 用户主动调用接口触发
}
```

### 2. 数据库字段

`chat_record_zip` 表新增字段：

| 字段 | 类型 | 说明 | 示例 |
|------|------|------|------|
| `persistence_type` | VARCHAR(20) | 持久化类型 | `auto` / `manual` |
| `persistence_time` | DATETIME | 持久化时间 | `2025-11-02 17:30:00` |

---

## 🔧 使用方式

### 1. 自动持久化（推荐）

**触发方式**：Redis 键过期事件自动触发

```java
// 1. 缓存消息时设置过期时间
cacheChatService.cacheChatMessage(
    conversationId,
    userMessage,
    botResponse,
    60  // 60 秒后自动持久化
);

// 2. 系统会自动：
//    - 设置过期标记键
//    - 60 秒后触发键过期事件
//    - 监听器捕获事件
//    - 自动执行持久化（类型：AUTO）
```

**日志输出**：
```
✓ 聊天消息已缓存，将在 60 秒后自动持久化: conversationId=test-001
🔔 检测到 Redis 键过期: chat:expire:test-001
⚡ 触发自动持久化: conversationId=test-001
📦 开始持久化对话记录: conversationId=test-001, 类型=自动持久化
✓ 批量保存聊天记录完成: conversationId=test-001, 总数=5, 类型=自动持久化
✓ 保存对话概要: conversationId=test-001, 成功=true, 标题=你好..., 类型=自动持久化
🎉 持久化完成: conversationId=test-001, 类型=自动持久化
```

---

### 2. 手动持久化

**触发方式**：用户主动调用接口

```java
// 手动触发持久化
cacheChatService.persistChatMessages(conversationId, PersistenceType.MANUAL);
```

**日志输出**：
```
📦 开始持久化对话记录: conversationId=test-002, 类型=手动持久化
✓ 批量保存聊天记录完成: conversationId=test-002, 总数=10, 类型=手动持久化
✓ 保存对话概要: conversationId=test-002, 成功=true, 标题=介绍Java..., 类型=手动持久化
🎉 持久化完成: conversationId=test-002, 类型=手动持久化
```

---

## 📊 数据库查询

### 查询所有持久化记录

```sql
SELECT 
    id,
    conversation_id,
    title,
    persistence_type,
    persistence_time
FROM chat_record_zip
ORDER BY persistence_time DESC;
```

### 查询自动持久化记录

```sql
SELECT 
    conversation_id,
    title,
    persistence_type,
    persistence_time
FROM chat_record_zip
WHERE persistence_type = 'auto'
ORDER BY persistence_time DESC;
```

### 查询手动持久化记录

```sql
SELECT 
    conversation_id,
    title,
    persistence_type,
    persistence_time
FROM chat_record_zip
WHERE persistence_type = 'manual'
ORDER BY persistence_time DESC;
```

### 统计持久化类型分布

```sql
SELECT 
    persistence_type,
    COUNT(*) as count,
    MIN(persistence_time) as first_time,
    MAX(persistence_time) as last_time
FROM chat_record_zip
GROUP BY persistence_type;
```

**示例结果**：
```
+------------------+-------+---------------------+---------------------+
| persistence_type | count | first_time          | last_time           |
+------------------+-------+---------------------+---------------------+
| auto             |   150 | 2025-11-01 10:00:00 | 2025-11-02 17:30:00 |
| manual           |    25 | 2025-11-01 11:00:00 | 2025-11-02 16:45:00 |
+------------------+-------+---------------------+---------------------+
```

---

## 🎨 使用场景

### 场景 1: 普通对话（自动持久化）

```java
// 用户发送消息
String conversationId = "conv-" + UUID.randomUUID();
cacheChatService.cacheChatMessage(
    conversationId,
    "你好，介绍一下 Java",
    "Java 是一种面向对象的编程语言...",
    300  // 5 分钟后自动持久化
);

// 5 分钟后系统自动持久化
// persistence_type = 'auto'
```

### 场景 2: 重要对话（手动持久化）

```java
// 用户完成重要对话后，立即手动持久化
String conversationId = "important-conv-001";

// 1. 缓存消息
cacheChatService.cacheChatMessage(
    conversationId,
    "请帮我生成项目架构方案",
    "以下是项目架构方案...",
    3600  // 设置较长过期时间
);

// 2. 用户点击"保存"按钮，立即手动持久化
cacheChatService.persistChatMessages(conversationId, PersistenceType.MANUAL);
// persistence_type = 'manual'
```

### 场景 3: 混合使用

```java
// 1. 普通对话使用自动持久化
cacheChatService.cacheChatMessage(conv1, msg1, resp1, 60);  // 自动

// 2. 重要对话使用手动持久化
cacheChatService.cacheChatMessage(conv2, msg2, resp2, 3600);
cacheChatService.persistChatMessages(conv2, PersistenceType.MANUAL);  // 手动

// 3. 数据库中可以区分
SELECT * FROM chat_record_zip WHERE persistence_type = 'manual';  // 重要对话
SELECT * FROM chat_record_zip WHERE persistence_type = 'auto';    // 普通对话
```

---

## 📈 监控和分析

### 1. 持久化效率分析

```sql
-- 每小时的持久化数量
SELECT 
    DATE_FORMAT(persistence_time, '%Y-%m-%d %H:00:00') as hour,
    persistence_type,
    COUNT(*) as count
FROM chat_record_zip
WHERE persistence_time >= DATE_SUB(NOW(), INTERVAL 24 HOUR)
GROUP BY hour, persistence_type
ORDER BY hour DESC;
```

### 2. 持久化延迟分析

```sql
-- 自动持久化的平均延迟（需要额外字段记录创建时间）
SELECT 
    AVG(TIMESTAMPDIFF(SECOND, created_at, persistence_time)) as avg_delay_seconds
FROM chat_record_zip
WHERE persistence_type = 'auto';
```

### 3. 用户行为分析

```sql
-- 手动持久化比例（反映用户对对话的重视程度）
SELECT 
    persistence_type,
    COUNT(*) as count,
    ROUND(COUNT(*) * 100.0 / (SELECT COUNT(*) FROM chat_record_zip), 2) as percentage
FROM chat_record_zip
GROUP BY persistence_type;
```

---

## 🔄 迁移步骤

### 步骤 1: 执行数据库迁移

```bash
# 连接数据库
mysql -u root -p your_database

# 执行迁移脚本
source /path/to/add_persistence_type.sql
```

### 步骤 2: 验证字段

```sql
DESC chat_record_zip;

-- 应该看到：
-- persistence_type    VARCHAR(20)    YES        manual
-- persistence_time    DATETIME       YES        CURRENT_TIMESTAMP
```

### 步骤 3: 更新已有数据（可选）

```sql
-- 将已有数据标记为手动持久化
UPDATE chat_record_zip 
SET persistence_type = 'manual',
    persistence_time = NOW()
WHERE persistence_type IS NULL;
```

### 步骤 4: 重启应用

```bash
mvn spring-boot:run
```

### 步骤 5: 测试功能

```bash
# 测试自动持久化
curl "http://localhost:8080/chat/push?message=测试自动&conversationId=auto-test&expireSeconds=10"

# 等待 10 秒，查看数据库
mysql> SELECT * FROM chat_record_zip WHERE conversation_id = 'auto-test';
# persistence_type 应该是 'auto'

# 测试手动持久化
curl "http://localhost:8080/chat/persist?conversationId=manual-test"

# 查看数据库
mysql> SELECT * FROM chat_record_zip WHERE conversation_id = 'manual-test';
# persistence_type 应该是 'manual'
```

---

## 📚 API 参考

### CacheChatService 接口

```java
public interface CacheChatService {
    /**
     * 缓存聊天消息
     * @param conversationId 对话ID
     * @param userMessage 用户消息
     * @param botResponse 机器人回复
     * @param expireSeconds 过期时间（秒）
     */
    boolean cacheChatMessage(String conversationId, String userMessage, 
                            String botResponse, long expireSeconds);

    /**
     * 持久化聊天消息
     * @param conversationId 对话ID
     * @param persistenceType 持久化类型（AUTO/MANUAL）
     */
    void persistChatMessages(String conversationId, PersistenceType persistenceType);
}
```

### PersistenceType 枚举

```java
public enum PersistenceType {
    AUTO("auto", "自动持久化"),
    MANUAL("manual", "手动持久化");
    
    public String getCode();           // 获取类型代码
    public String getDescription();    // 获取类型描述
    public static PersistenceType fromCode(String code);  // 根据代码获取枚举
}
```

---

## ✅ 验收清单

- [x] 创建 `PersistenceType` 枚举
- [x] 修改 `ChatRecordZip` 实体类
- [x] 创建数据库迁移脚本
- [x] 修改 `CacheChatService` 接口
- [x] 修改 `ChatRecordService` 接口
- [x] 修改 `RedisCacheChatService` 实现
- [x] 修改 `DefaultCacheChatServiceImpl` 实现
- [x] 修改 `RedisKeyExpirationListener` 监听器
- [x] 创建使用指南文档
- [ ] 执行数据库迁移
- [ ] 测试自动持久化
- [ ] 测试手动持久化
- [ ] 验证数据库记录

---

## 🎉 总结

### 改进点

1. ✅ **类型区分**: 清晰区分自动和手动持久化
2. ✅ **时间记录**: 记录精确的持久化时间
3. ✅ **数据分析**: 支持持久化行为分析
4. ✅ **用户洞察**: 了解用户对对话的重视程度
5. ✅ **系统监控**: 监控持久化效率和延迟

### 使用建议

- **普通对话**: 使用自动持久化（60-300秒）
- **重要对话**: 使用手动持久化（立即保存）
- **临时对话**: 使用较长过期时间（3600秒），让用户决定是否保存

---

**版本**: v1.0  
**完成日期**: 2025-11-02  
**状态**: ✅ 已完成，待测试
