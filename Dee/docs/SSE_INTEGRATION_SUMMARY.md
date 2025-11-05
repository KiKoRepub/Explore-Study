# SSE 集成总结

## 改动概述

本次更新将所有 AI 对话接口与 SSE (Server-Sent Events) 进行了集成，实现了实时流式响应功能。

## 新增文件

### 1. SSEController.java
**路径**: `src/main/java/org/dee/controller/SSEController.java`

**功能**: SSE 连接管理控制器

**接口**:
- `GET /sse/connect?userId={userId}` - 建立 SSE 连接
- `DELETE /sse/disconnect?userId={userId}` - 断开连接
- `GET /sse/status?userId={userId}` - 检查连接状态
- `GET /sse/count` - 获取当前连接数

### 2. SSEResponseHelper.java
**路径**: `src/main/java/org/dee/util/SSEResponseHelper.java`

**功能**: SSE 响应工具类，统一处理流式响应

**主要方法**:
- `handleStreamResponse()` - 处理流式对话响应
- `sendChunkMessage()` - 发送消息块
- `sendErrorMessage()` - 发送错误消息
- `sendCompleteMessage()` - 发送完成消息
- `checkConnectionAndGetResult()` - 检查连接状态

### 3. SSE_INTEGRATION_GUIDE.md
**路径**: `docs/SSE_INTEGRATION_GUIDE.md`

**内容**: 完整的 SSE 集成使用指南，包含：
- 架构说明
- 使用流程
- 完整的前端示例代码
- API 接口文档
- 故障排查指南

## 修改文件

### 1. SSEServer.java
**路径**: `src/main/java/org/dee/sse/SSEServer.java`

**改动**:
- ✅ 修改 `connect()` 方法返回 `SseEmitter` 对象
- ✅ 新增 `getEmitter()` - 获取指定用户的连接
- ✅ 新增 `sendMessage()` - 发送消息（重载方法）
- ✅ 新增 `sendComplete()` - 发送完成信号
- ✅ 新增 `isConnected()` - 检查连接状态
- ✅ 新增 `getConnectionCount()` - 获取连接数
- ✅ 完善日志记录

### 2. ChatController.java
**路径**: `src/main/java/org/dee/controller/ChatController.java`

**改动**:
- ✅ 新增 `@Slf4j` 注解，添加日志支持
- ✅ 导入 SSE 相关类和 Reactor 依赖
- ✅ 新增 `/chat/push/stream` 接口 - 流式对话（带记忆）
- ✅ 新增 `/chat/tool/stream` 接口 - 流式对话（带工具）
- ✅ 保留原有接口，向后兼容

## 新增接口详情

### 1. 流式对话（带记忆）

**接口**: `GET /chat/push/stream`

**参数**:
- `message` (必需) - 用户消息
- `userId` (必需) - 用户唯一标识
- `conversationId` (可选) - 对话ID，不提供则自动生成
- `expireSeconds` (可选，默认3600) - 缓存过期时间（秒）

**返回**:
```json
{
  "status": "processing",
  "conversationId": "uuid",
  "message": "对话处理中，请通过SSE接收响应"
}
```

**SSE 消息格式**:
- 消息块: `{ "type": "chunk", "content": "...", "conversationId": "...", "timestamp": 123 }`
- 完成: `{ "type": "complete", "conversationId": "...", "fullResponse": "...", "timestamp": 123 }`
- 错误: `{ "type": "error", "message": "...", "conversationId": "...", "timestamp": 123 }`

### 2. 流式对话（带工具）

**接口**: `GET /chat/tool/stream`

**参数**: 同上

**功能**: 
- 自动加载所有启用的工具
- 支持工具调用
- 流式返回响应

## 技术特点

### 1. 异步处理
- 使用独立线程处理对话，不阻塞主线程
- 立即返回响应，通过 SSE 推送结果

### 2. 流式响应
- 使用 Spring AI 的 `Flux` 流式API
- 逐块推送响应内容
- 实现打字机效果

### 3. 错误处理
- 完善的异常捕获机制
- 通过 SSE 推送错误信息
- 自动清理异常连接

### 4. 连接管理
- 使用 `ConcurrentHashMap` 管理连接
- 支持连接超时、错误、完成回调
- 自动清理断开的连接

### 5. 向后兼容
- 保留所有原有接口
- 新增流式接口作为补充
- 不影响现有功能

## 使用流程

### 后端启动
1. 启动 Spring Boot 应用
2. SSE 服务自动可用

### 前端集成
1. 建立 SSE 连接: `GET /sse/connect?userId={userId}`
2. 监听 SSE 事件: `connected`, `message`, `error`
3. 发送对话请求: `GET /chat/push/stream?message=...&userId=...`
4. 接收流式响应: 通过 SSE 事件接收
5. 对话完成: 收到 `complete` 事件
6. 断开连接: 关闭 `EventSource`

## 前端示例

```javascript
// 1. 建立连接
const eventSource = new EventSource(`/sse/connect?userId=${userId}`);

// 2. 监听消息
eventSource.addEventListener('message', (event) => {
    const data = JSON.parse(event.data);
    if (data.type === 'chunk') {
        // 显示消息块
        appendText(data.content);
    } else if (data.type === 'complete') {
        // 对话完成
        console.log('完整响应:', data.fullResponse);
    }
});

// 3. 发送请求
fetch(`/chat/push/stream?message=${msg}&userId=${userId}`)
    .then(res => res.json())
    .then(result => console.log(result));
```

完整示例请参考 `docs/SSE_INTEGRATION_GUIDE.md`

## 依赖要求

### Maven 依赖
```xml
<!-- Spring Web (已有) -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>

<!-- Spring AI (已有) -->
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-core</artifactId>
</dependency>

<!-- Reactor Core (已有) -->
<dependency>
    <groupId>io.projectreactor</groupId>
    <artifactId>reactor-core</artifactId>
</dependency>

<!-- Lombok (已有) -->
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
</dependency>
```

## 配置说明

### CORS 配置（如需跨域）
```java
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE")
                .allowedHeaders("*");
    }
}
```

### SSE 超时配置
默认设置为永不超时 (`0L`)，可在 `SSEServer.connect()` 中修改：
```java
SseEmitter emitter = new SseEmitter(30_000L); // 30秒超时
```

## 测试建议

### 1. 单元测试
- 测试 SSE 连接建立和断开
- 测试消息发送功能
- 测试并发连接

### 2. 集成测试
- 测试完整对话流程
- 测试工具调用
- 测试错误处理

### 3. 性能测试
- 测试并发连接数
- 测试长时间连接稳定性
- 测试消息推送延迟

## 监控和日志

### 日志级别
- `INFO`: 连接建立、断开、对话开始/完成
- `WARN`: 连接异常、用户未连接
- `ERROR`: 发送失败、处理异常

### 监控指标
- 当前连接数: `GET /sse/count`
- 连接状态: `GET /sse/status?userId={userId}`

## 注意事项

1. **连接管理**
   - 每个 userId 只能有一个活跃连接
   - 新连接会自动替换旧连接
   - 记得在页面关闭时断开连接

2. **消息格式**
   - 所有 SSE 消息都是 JSON 格式
   - 需要使用 `JSON.parse()` 解析

3. **浏览器兼容性**
   - 现代浏览器都支持 EventSource
   - IE 不支持，需要 polyfill

4. **性能考虑**
   - 对话处理在异步线程中执行
   - 避免同时发起大量请求
   - 考虑添加请求限流

5. **安全性**
   - 建议添加用户认证
   - 验证 userId 合法性
   - 防止恶意连接

## 后续优化建议

1. **功能增强**
   - [ ] 添加消息队列，支持离线消息
   - [ ] 实现自动重连机制
   - [ ] 支持多会话管理
   - [ ] 添加消息历史记录

2. **性能优化**
   - [ ] 使用线程池管理异步任务
   - [ ] 添加连接数限制
   - [ ] 实现消息批量发送
   - [ ] 添加缓存机制

3. **监控完善**
   - [ ] 添加 Prometheus 指标
   - [ ] 实现健康检查接口
   - [ ] 添加性能监控
   - [ ] 实现告警机制

4. **安全加固**
   - [ ] 添加 JWT 认证
   - [ ] 实现请求限流
   - [ ] 添加 IP 白名单
   - [ ] 实现消息加密

## 总结

本次 SSE 集成实现了：
- ✅ 实时流式响应
- ✅ 完整的连接管理
- ✅ 统一的消息格式
- ✅ 完善的错误处理
- ✅ 向后兼容
- ✅ 详细的文档

所有 AI 对话接口现在都支持通过 SSE 进行实时通信，可以为前端提供更好的用户体验。
