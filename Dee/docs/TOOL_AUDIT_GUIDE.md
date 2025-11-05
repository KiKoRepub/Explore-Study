# 工具调用审计系统使用指南

## 概述

工具调用审计系统自动记录所有工具的调用情况，包括参数、结果、执行时间、成功/失败状态等信息，便于监控、分析和故障排查。

## 功能特性

- ✅ **自动审计**：通过 AOP 自动拦截所有 `@Tool` 注解的方法
- ✅ **详细记录**：记录参数、结果、执行时间、错误信息等
- ✅ **多维查询**：支持按对话ID、工具名称、时间范围等查询
- ✅ **统计分析**：提供工具使用统计、成功率分析等功能
- ✅ **历史清理**：支持定期清理历史审计日志

## 数据库表结构

### tool_audit_log 表

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | INT | 主键，自增 |
| conversation_id | VARCHAR(255) | 对话ID |
| request_id | VARCHAR(255) | 请求ID（唯一标识每次调用） |
| tool_name | VARCHAR(255) | 工具名称（类名） |
| method_name | VARCHAR(255) | 方法名称 |
| status | VARCHAR(50) | 执行状态：STARTED, COMPLETED, FAILED |
| parameters | TEXT | 工具参数（JSON格式） |
| result | TEXT | 执行结果（JSON格式） |
| error_message | TEXT | 错误信息 |
| execution_time | BIGINT | 执行耗时（毫秒） |
| created_at | DATETIME | 创建时间 |
| start_at | DATETIME | 开始时间 |
| end_at | DATETIME | 结束时间 |
| user_id | VARCHAR(255) | 用户ID |
| ip_address | VARCHAR(50) | IP地址 |

## 初始化步骤

### 1. 创建数据库表

执行 `src/main/resources/sql/tool_audit_log.sql` 文件：

```sql
CREATE TABLE IF NOT EXISTS `tool_audit_log` (
    `id` INT AUTO_INCREMENT PRIMARY KEY COMMENT '日志ID',
    `conversation_id` VARCHAR(255) NOT NULL COMMENT '对话ID',
    `request_id` VARCHAR(255) NOT NULL COMMENT '请求ID',
    `tool_name` VARCHAR(255) NOT NULL COMMENT '工具名称',
    `method_name` VARCHAR(255) NOT NULL COMMENT '方法名称',
    `status` VARCHAR(50) NOT NULL COMMENT '执行状态',
    `parameters` TEXT COMMENT '工具参数(JSON格式)',
    `result` TEXT COMMENT '执行结果(JSON格式)',
    `error_message` TEXT COMMENT '错误信息',
    `execution_time` BIGINT COMMENT '执行耗时(毫秒)',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `start_at` DATETIME,
    `end_at` DATETIME,
    `user_id` VARCHAR(255),
    `ip_address` VARCHAR(50),
    INDEX `idx_conversation_id` (`conversation_id`),
    INDEX `idx_tool_name` (`tool_name`),
    INDEX `idx_status` (`status`),
    INDEX `idx_created_at` (`created_at`),
    INDEX `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

### 2. 启动应用

AOP 切面会自动生效，无需额外配置。

## API 接口说明

### 1. 根据对话ID查询审计日志

**接口**: `GET /tool-audit/conversation/{conversationId}`

**请求示例**:
```bash
curl -X GET http://localhost:8080/tool-audit/conversation/conv-123
```

**响应示例**:
```json
{
    "code": 200,
    "message": "success",
    "data": [
        {
            "id": 1,
            "conversationId": "conv-123",
            "requestId": "req-456",
            "toolName": "WeatherTool",
            "methodName": "getCurrentWeather",
            "status": "COMPLETED",
            "parameters": "[\"北京\"]",
            "result": "城市：北京\n天气：晴天\n温度：20°C",
            "executionTime": 125,
            "createdAt": "2025-11-05T17:30:00",
            "startAt": "2025-11-05T17:30:00",
            "endAt": "2025-11-05T17:30:00.125",
            "userId": "user-001",
            "ipAddress": "192.168.1.100"
        }
    ]
}
```

### 2. 根据工具名称查询审计日志

**接口**: `GET /tool-audit/tool/{toolName}?limit=100`

**请求示例**:
```bash
curl -X GET "http://localhost:8080/tool-audit/tool/WeatherTool?limit=50"
```

### 3. 查询指定时间范围内的审计日志

**接口**: `GET /tool-audit/time-range`

**请求参数**:
- `startTime`: 开始时间（格式：yyyy-MM-dd HH:mm:ss）
- `endTime`: 结束时间（格式：yyyy-MM-dd HH:mm:ss）

**请求示例**:
```bash
curl -X GET "http://localhost:8080/tool-audit/time-range?startTime=2025-11-05 00:00:00&endTime=2025-11-05 23:59:59"
```

### 4. 查询失败的工具调用

**接口**: `GET /tool-audit/failed?limit=50`

**请求示例**:
```bash
curl -X GET "http://localhost:8080/tool-audit/failed?limit=20"
```

### 5. 统计工具调用次数

**接口**: `GET /tool-audit/count/{toolName}`

**请求参数**:
- `startTime`: 开始时间（可选）
- `endTime`: 结束时间（可选）

**请求示例**:
```bash
curl -X GET "http://localhost:8080/tool-audit/count/WeatherTool?startTime=2025-11-05 00:00:00&endTime=2025-11-05 23:59:59"
```

**响应示例**:
```json
{
    "code": 200,
    "message": "success",
    "data": {
        "toolName": "WeatherTool",
        "count": 156,
        "startTime": "2025-11-05 00:00:00",
        "endTime": "2025-11-05 23:59:59"
    }
}
```

### 6. 获取工具使用统计

**接口**: `GET /tool-audit/statistics`

**请求参数**:
- `startTime`: 开始时间
- `endTime`: 结束时间

**请求示例**:
```bash
curl -X GET "http://localhost:8080/tool-audit/statistics?startTime=2025-11-05 00:00:00&endTime=2025-11-05 23:59:59"
```

**响应示例**:
```json
{
    "code": 200,
    "message": "success",
    "data": {
        "totalCalls": 500,
        "successCount": 475,
        "failureCount": 25,
        "avgExecutionTime": 156.8,
        "successRate": "95.00%",
        "toolUsageCount": {
            "WeatherTool": 200,
            "CalculatorTool": 150,
            "ImageTool": 150
        }
    }
}
```

### 7. 获取最近24小时统计

**接口**: `GET /tool-audit/statistics/recent`

**请求示例**:
```bash
curl -X GET http://localhost:8080/tool-audit/statistics/recent
```

### 8. 清理历史审计日志

**接口**: `DELETE /tool-audit/cleanup`

**请求参数**:
- `beforeTime`: 删除此时间之前的日志

**请求示例**:
```bash
curl -X DELETE "http://localhost:8080/tool-audit/cleanup?beforeTime=2025-10-01 00:00:00"
```

**响应示例**:
```json
{
    "code": 200,
    "message": "成功删除 1250 条记录",
    "data": {
        "deletedCount": 1250,
        "beforeTime": "2025-10-01 00:00:00"
    }
}
```

## 工作原理

### AOP 自动审计

`ToolAuditAspect` 切面会自动拦截所有带 `@Tool` 注解的方法：

```java
@Around("@annotation(org.springframework.ai.tool.annotation.Tool)")
public Object auditToolExecution(ProceedingJoinPoint joinPoint) throws Throwable {
    // 1. 记录调用开始
    Integer logId = toolAuditService.logToolStart(...);
    
    try {
        // 2. 执行目标方法
        Object result = joinPoint.proceed();
        
        // 3. 记录成功
        toolAuditService.logToolSuccess(logId, result);
        
        return result;
    } catch (Throwable throwable) {
        // 4. 记录失败
        toolAuditService.logToolFailure(logId, errorMessage);
        throw throwable;
    }
}
```

### 审计信息来源

- **conversationId**: 从请求头 `X-Conversation-Id` 或参数 `conversationId` 获取
- **userId**: 从请求头 `X-User-Id` 或参数 `userId` 获取
- **ipAddress**: 从请求头 `X-Forwarded-For`、`X-Real-IP` 或 `RemoteAddr` 获取

## 使用场景

### 1. 监控工具调用情况

```bash
# 查看最近24小时的统计
curl -X GET http://localhost:8080/tool-audit/statistics/recent
```

### 2. 排查工具调用失败

```bash
# 查看最近失败的调用
curl -X GET "http://localhost:8080/tool-audit/failed?limit=10"
```

### 3. 分析工具性能

```bash
# 查看特定工具的调用记录
curl -X GET "http://localhost:8080/tool-audit/tool/WeatherTool?limit=100"
```

### 4. 追踪对话中的工具使用

```bash
# 查看某个对话中使用了哪些工具
curl -X GET http://localhost:8080/tool-audit/conversation/conv-123
```

### 5. 定期清理历史数据

```bash
# 删除30天前的审计日志
curl -X DELETE "http://localhost:8080/tool-audit/cleanup?beforeTime=2025-10-05 00:00:00"
```

## 高级用法

### 1. 在 ChatController 中传递对话ID

```java
@GetMapping("/chat/with-tools")
public String chatWithTools(
        @RequestParam String message,
        @RequestParam String conversationId,
        HttpServletRequest request) {
    
    // 设置对话ID到请求头（供 AOP 切面使用）
    request.setAttribute("X-Conversation-Id", conversationId);
    
    // 使用工具进行对话
    ToolCallback[] tools = toolService.getEnabledToolCallbacks();
    ChatResponse response = chatClient.prompt()
            .user(message)
            .tools(tools)
            .call()
            .chatResponse();
    
    return response.getResult().getOutput().getText();
}
```

### 2. 自定义审计信息

如果需要自定义审计信息，可以直接调用 `ToolAuditService`：

```java
@Autowired
private ToolAuditService toolAuditService;

public void customToolCall() {
    // 记录开始
    Integer logId = toolAuditService.logToolStart(
        "conv-123", "req-456", "CustomTool", "customMethod",
        "{\"param\": \"value\"}", "user-001", "192.168.1.1"
    );
    
    try {
        // 执行操作
        String result = doSomething();
        
        // 记录成功
        toolAuditService.logToolSuccess(logId, result);
    } catch (Exception e) {
        // 记录失败
        toolAuditService.logToolFailure(logId, e.getMessage());
    }
}
```

### 3. 定时清理任务

创建定时任务自动清理历史数据：

```java
@Component
public class AuditCleanupTask {
    
    @Autowired
    private ToolAuditService toolAuditService;
    
    @Scheduled(cron = "0 0 2 * * ?") // 每天凌晨2点执行
    public void cleanupOldLogs() {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        Integer deletedCount = toolAuditService.deleteLogsBefore(thirtyDaysAgo);
        System.out.println("清理了 " + deletedCount + " 条审计日志");
    }
}
```

## 性能优化

### 1. 异步审计

如果审计影响性能，可以改为异步：

```java
@Async
public void logToolStartAsync(...) {
    toolAuditService.logToolStart(...);
}
```

### 2. 批量插入

对于高并发场景，可以使用批量插入：

```java
List<ToolAuditLog> logs = new ArrayList<>();
// 收集日志...
toolAuditLogMapper.batchInsert(logs);
```

### 3. 数据库索引

确保已创建必要的索引（建表脚本中已包含）：
- `idx_conversation_id`
- `idx_tool_name`
- `idx_status`
- `idx_created_at`
- `idx_user_id`

## 注意事项

1. **数据量管理**：定期清理历史数据，避免表过大影响性能
2. **敏感信息**：注意参数和结果中可能包含敏感信息，需要脱敏处理
3. **性能影响**：AOP 会增加少量开销，如有性能要求可考虑异步审计
4. **存储空间**：审计日志会占用存储空间，需要定期清理或归档

## 故障排查

### 问题 1: 审计日志未生成

**可能原因**:
- AOP 切面未生效
- 工具方法未使用 `@Tool` 注解
- 数据库连接失败

**解决方案**:
- 检查 `@EnableAspectJAutoProxy` 是否启用
- 确认工具方法有 `@Tool` 注解
- 检查数据库连接配置

### 问题 2: 参数序列化失败

**可能原因**:
- 参数对象无法序列化为 JSON

**解决方案**:
- 确保参数对象实现了 Serializable 或有合适的 JSON 序列化器
- 检查 ObjectMapper 配置

### 问题 3: 查询性能慢

**可能原因**:
- 数据量过大
- 缺少索引

**解决方案**:
- 定期清理历史数据
- 确认索引已创建
- 使用分页查询

## 扩展功能

可以根据需要扩展以下功能：

- 审计日志导出（Excel、CSV）
- 实时监控告警
- 工具调用链路追踪
- 审计日志可视化
- 异常工具自动禁用
