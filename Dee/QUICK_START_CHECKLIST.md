# 🚀 Redis 自动持久化功能 - 快速启动检查清单

## ✅ 启动前检查

### 1. Redis 服务
```bash
# 检查 Redis 是否运行
redis-cli ping
# 期望输出: PONG

# 检查 Redis 版本（需要 2.8.0+）
redis-cli info server | grep redis_version
```

### 2. 启用主类异步支持
确保主类添加了 `@EnableAsync` 注解：

```java
@SpringBootApplication
@EnableAsync  // ← 必须添加
public class DeeApplication {
    public static void main(String[] args) {
        SpringApplication.run(DeeApplication.class, args);
    }
}
```

### 3. 数据库表结构
确保以下表已创建：

```sql
-- 检查表是否存在
SHOW TABLES LIKE 'chat_record%';

-- 应该看到:
-- chat_record
-- chat_record_zip
```

---

## 🎬 启动应用

### 启动命令
```bash
# Maven
mvn spring-boot:run

# 或 Gradle
gradle bootRun

# 或直接运行 JAR
java -jar dee-application.jar
```

### 期望日志输出
启动时应该看到以下关键日志：

```
✓ ——————————————本地Redis 连接成功—————————————————
✓ Redis 键空间通知已启用: notify-keyspace-events=Ex
✓ 自动持久化功能已激活
✓ Redis 消息监听容器已配置
✓ Redis 键过期监听器已启动
```

**如果看到这些日志，说明配置成功！** ✅

---

## 🧪 快速测试

### 测试 1: 基本功能（60秒自动持久化）

```bash
# 1. 发送第一条消息
curl "http://localhost:8080/chat/push?message=你好&conversationId=test-001&expireSeconds=60"

# 2. 发送第二条消息
curl "http://localhost:8080/chat/push?message=介绍Java&conversationId=test-001&expireSeconds=60"

# 3. 查看日志，应该看到:
# ✓ 聊天消息已缓存，将在 60 秒后自动持久化: conversationId=test-001
# 设置过期标记: conversationId=test-001, 过期时间=60秒

# 4. 等待 60 秒后，查看日志，应该看到:
# 🔔 检测到 Redis 键过期: chat:expire:test-001
# ⚡ 触发自动持久化: conversationId=test-001
# 📦 开始持久化对话记录: conversationId=test-001
# ✓ 批量保存聊天记录完成: conversationId=test-001, 总数=2
# 🎉 持久化完成: conversationId=test-001
```

### 测试 2: 手动持久化

```bash
# 1. 发送消息
curl "http://localhost:8080/chat/push?message=测试&conversationId=manual-001&expireSeconds=3600"

# 2. 立即手动触发持久化
curl -X POST "http://localhost:8080/chat/persist?conversationId=manual-001"

# 3. 应该返回: "持久化成功"
```

### 测试 3: 验证数据库

```sql
-- 查看聊天记录
SELECT * FROM chat_record WHERE conversation_id = 'test-001';

-- 查看对话摘要
SELECT * FROM chat_record_zip WHERE conversation_id = 'test-001';
```

---

## 🔍 验证 Redis 状态

```bash
# 1. 查看所有聊天记录键
redis-cli --scan --pattern "spring_ai_alibaba_chat_memory:*"

# 2. 查看所有过期标记键
redis-cli --scan --pattern "chat:expire:*"

# 3. 查看特定键的 TTL
redis-cli ttl "chat:expire:test-001"
# 返回剩余秒数，-2 表示键不存在，-1 表示永不过期

# 4. 查看特定对话的消息
redis-cli lrange "spring_ai_alibaba_chat_memory:test-001" 0 -1
```

---

## ❌ 常见问题快速修复

### 问题 1: 日志中没有 "Redis 键空间通知已启用"

**修复**:
```bash
redis-cli config set notify-keyspace-events Ex
```

### 问题 2: 日志中没有 "Redis 键过期监听器已启动"

**修复**:
```java
// 确保主类有 @EnableAsync
@SpringBootApplication
@EnableAsync
public class DeeApplication { }
```

### 问题 3: 60秒后没有触发持久化

**检查**:
```bash
# 1. 检查键空间通知
redis-cli config get notify-keyspace-events
# 应返回: "Ex"

# 2. 检查过期标记键是否存在
redis-cli exists "chat:expire:test-001"
# 返回 1 表示存在，0 表示不存在或已过期

# 3. 查看应用日志是否有错误
```

### 问题 4: 数据库中没有数据

**检查**:
```sql
-- 1. 检查表结构
DESC chat_record;

-- 2. 查看最近的记录
SELECT * FROM chat_record ORDER BY created_at DESC LIMIT 10;

-- 3. 检查应用日志中的错误信息
```

---

## 📊 监控仪表板（可选）

### 关键指标

```bash
# Redis 内存使用
redis-cli info memory | grep used_memory_human

# Redis 键数量
redis-cli dbsize

# 数据库记录数
mysql> SELECT COUNT(*) FROM chat_record;
mysql> SELECT COUNT(*) FROM chat_record_zip;
```

---

## 🎯 成功标准

如果以下所有项都通过，说明系统运行正常：

- [x] 应用启动日志显示所有组件已启动
- [x] 发送消息后 Redis 中有对应的键
- [x] 过期标记键设置成功
- [x] 60秒后触发自动持久化
- [x] 数据库中有对应的记录
- [x] Redis 缓存已清理

---

## 📞 获取帮助

如果遇到问题：

1. **查看完整文档**: `AUTO_PERSISTENCE_GUIDE.md`
2. **查看修复总结**: `FIXES_SUMMARY.md`
3. **查看应用日志**: 搜索关键词 "持久化"、"Redis"、"过期"
4. **检查 Redis 日志**: `redis-cli monitor`

---

## 🎉 恭喜！

如果所有测试都通过，你已经成功配置了 Redis 自动持久化功能！

**下一步**:
- 根据实际需求调整过期时间
- 配置监控和告警
- 优化性能参数
- 添加更多测试用例

---

**快速启动版本**: v1.0  
**最后更新**: 2025-11-02
