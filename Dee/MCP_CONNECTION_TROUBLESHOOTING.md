# MCP 连接问题诊断与解决方案

## 🔴 问题现象
Dee模块启动后无法连接到Mcp-Server模块的MCP服务

## 🔍 问题分析

### 1. **配置对比**

#### Mcp-Server 配置 (端口 8085)
```yaml
spring:
  ai:
    mcp:
      server:
        type: ASYNC
        name: mcp-server
        sse-message-endpoint: /mcp/book
        enabled: true
```

#### Dee 客户端配置
```yaml
spring:
  ai:
    mcp:
      client:
        enabled: true
        sse:
          connections:
            server1:
              url: http://localhost:8085
              sse-endpoint: /mcp/book
```

### 2. **可能的问题原因**

#### ❌ 问题1: MCP Server 使用了 webmvc，Client 使用了 webflux
- **Mcp-Server**: `spring-ai-starter-mcp-server-webmvc` (同步阻塞)
- **Dee Client**: `spring-ai-starter-mcp-client-webflux` (异步非阻塞)

**这可能导致协议不兼容！**

#### ❌ 问题2: SSE端点可能没有正确暴露
MCP Server需要确保SSE端点正确配置并可访问

#### ❌ 问题3: 缺少WebFlux依赖
如果使用webflux客户端，需要确保有Spring WebFlux的依赖

#### ❌ 问题4: 防火墙或网络问题
本地8085端口可能被占用或防火墙阻止

## ✅ 解决方案

### 方案1: 统一使用 WebFlux (推荐)

#### 1.1 修改 Mcp-Server 的 pom.xml

**替换依赖：**
```xml
<!-- 移除 webmvc -->
<!--
<dependency>
  <groupId>org.springframework.ai</groupId>
  <artifactId>spring-ai-starter-mcp-server-webmvc</artifactId>
  <version>1.0.0</version>
</dependency>
-->

<!-- 改用 webflux -->
<dependency>
  <groupId>org.springframework.ai</groupId>
  <artifactId>spring-ai-starter-mcp-server-webflux</artifactId>
  <version>1.0.0</version>
</dependency>
```

#### 1.2 修改 Mcp-Server 的 application.yml

```yaml
server:
  port: 8085
spring:
  application:
    name: mcp-server
  ai:
    mcp:
      server:
        type: ASYNC  # 保持异步
        name: mcp-server
        version: "1.0.0"
        base-url: http://localhost:8085
        enabled: true
        sse-message-endpoint: /mcp/book
        capabilities:
          tool: true
          resource: false
          prompt: false
```

### 方案2: 统一使用 WebMVC

#### 2.1 修改 Dee 的 pom.xml

**替换依赖：**
```xml
<!-- 移除 webflux -->
<!--
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-mcp-client-webflux</artifactId>
</dependency>
-->

<!-- 改用 webmvc -->
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-mcp-client-webmvc</artifactId>
</dependency>
```

### 方案3: 检查和修复配置问题

#### 3.1 确保 Mcp-Server 正确启动

**添加日志配置到 application.yml:**
```yaml
logging:
  level:
    org.springframework.ai.mcp: DEBUG
    org.mcp: DEBUG
```

#### 3.2 验证 SSE 端点是否可访问

在浏览器或使用curl测试：
```bash
curl -N http://localhost:8085/mcp/book
```

应该返回SSE流或MCP协议响应。

#### 3.3 在 Dee 中添加调试日志

**修改 application.yaml:**
```yaml
logging:
  level:
    org.springframework.ai.mcp: DEBUG
    io.modelcontextprotocol: DEBUG
```

### 方案4: 添加健康检查端点

#### 4.1 在 Mcp-Server 中添加健康检查

创建 `HealthController.java`:
```java
package org.mcp.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {
    
    @GetMapping("/health")
    public String health() {
        return "MCP Server is running!";
    }
    
    @GetMapping("/mcp/status")
    public String mcpStatus() {
        return "MCP endpoint is ready at /mcp/book";
    }
}
```

#### 4.2 测试连接
```bash
# 测试基本连接
curl http://localhost:8085/health

# 测试MCP状态
curl http://localhost:8085/mcp/status
```

## 🔧 调试步骤

### 1. 启动顺序
```bash
# 1. 先启动 Mcp-Server
cd Mcp-Server
mvn spring-boot:run

# 2. 等待启动完成，查看日志确认端口8085已监听

# 3. 再启动 Dee
cd Dee
mvn spring-boot:run

# 4. 查看 Dee 的启动日志，检查MCP连接信息
```

### 2. 检查日志关键信息

**Mcp-Server 启动日志应包含：**
```
Started McpServerApplication in X seconds
Tomcat started on port(s): 8085
MCP Server enabled at /mcp/book
```

**Dee 启动日志应包含：**
```
Initializing MCP clients
Connected to MCP server: http://localhost:8085/mcp/book
Registered X MCP tools
```

### 3. 使用工具测试连接

**使用 Postman 或 curl 测试 SSE 连接：**
```bash
curl -N -H "Accept: text/event-stream" http://localhost:8085/mcp/book
```

## 📝 配置检查清单

- [ ] Mcp-Server 在 8085 端口成功启动
- [ ] SSE端点 `/mcp/book` 可访问
- [ ] Dee 和 Mcp-Server 使用兼容的依赖（都用webflux或都用webmvc）
- [ ] 防火墙没有阻止 8085 端口
- [ ] 配置文件中的 URL 和端点路径完全匹配
- [ ] 两个应用都启用了 MCP 功能 (enabled: true)
- [ ] 日志级别设置为 DEBUG 以便查看详细信息

## 🚀 推荐配置（最佳实践）

### Mcp-Server pom.xml
```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-mcp-server-webflux</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Mcp-Server application.yml
```yaml
server:
  port: 8085

spring:
  application:
    name: mcp-server
  ai:
    mcp:
      server:
        type: ASYNC
        name: book-mcp-server
        version: "1.0.0"
        enabled: true
        sse-message-endpoint: /mcp/book
        capabilities:
          tool: true

logging:
  level:
    org.springframework.ai.mcp: DEBUG
```

### Dee pom.xml
```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-mcp-client-webflux</artifactId>
</dependency>
```

### Dee mcp.yml
```yaml
spring:
  ai:
    mcp:
      client:
        enabled: true
        name: mcp-client
        toolcallback:
          enabled: true
        sse:
          connections:
            bookServer:
              url: http://localhost:8085
              sse-endpoint: /mcp/book
        request-timeout: 60000

logging:
  level:
    org.springframework.ai.mcp: DEBUG
    io.modelcontextprotocol: DEBUG
```

## 🎯 快速验证

运行以下命令快速验证MCP连接：

```bash
# 1. 检查 Mcp-Server 是否运行
netstat -ano | findstr :8085

# 2. 测试 SSE 端点
curl -v http://localhost:8085/mcp/book

# 3. 查看 Dee 的 MCP 客户端日志
# 在启动日志中搜索 "MCP" 关键字
```

## 💡 常见错误信息及解决

### 错误1: Connection refused
**原因**: Mcp-Server 未启动或端口错误
**解决**: 确保 Mcp-Server 先启动，检查端口配置

### 错误2: 404 Not Found
**原因**: SSE端点路径配置错误
**解决**: 确保 `sse-message-endpoint` 和 `sse-endpoint` 路径一致

### 错误3: Protocol mismatch
**原因**: WebMVC 和 WebFlux 混用
**解决**: 统一使用相同的技术栈

### 错误4: Timeout
**原因**: 网络延迟或服务响应慢
**解决**: 增加 `request-timeout` 配置值
