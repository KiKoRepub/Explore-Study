# MCP 连接问题 - 快速修复指南

## 🎯 核心问题

**Mcp-Server 使用 `webmvc`，Dee 使用 `webflux`，导致协议不兼容！**

## ✅ 推荐解决方案：统一使用 WebFlux

### 步骤 1: 修改 Mcp-Server 的 pom.xml

**位置**: `d:\university\JAVA\Explore-Study\Mcp-Server\pom.xml`

**修改前 (第 31-35 行):**
```xml
<dependency>
  <groupId>org.springframework.ai</groupId>
  <artifactId>spring-ai-starter-mcp-server-webmvc</artifactId>
  <version>1.0.0</version>
</dependency>
```

**修改后:**
```xml
<dependency>
  <groupId>org.springframework.ai</groupId>
  <artifactId>spring-ai-starter-mcp-server-webflux</artifactId>
  <version>1.0.0</version>
</dependency>
```

### 步骤 2: 优化 Mcp-Server 的 application.yml

**位置**: `d:\university\JAVA\Explore-Study\Mcp-Server\src\main\resources\application.yml`

**完整配置:**
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
        base-url: http://localhost:8085
        enabled: true
        sse-message-endpoint: /mcp/book
        capabilities:
          tool: true
          resource: false
          prompt: false
          completion: false

# 添加调试日志
logging:
  level:
    org.springframework.ai.mcp: DEBUG
    org.mcp: DEBUG
```

### 步骤 3: 优化 Dee 的 mcp.yml

**位置**: `d:\university\JAVA\Explore-Study\Dee\src\main\resources\config\mcp.yml`

**完整配置:**
```yaml
spring:
  ai:
    mcp:
      client:
        enabled: true
        name: dee-mcp-client
        toolcallback:
          enabled: true
        sse:
          connections:
            bookServer:
              url: http://localhost:8085
              sse-endpoint: /mcp/book
            # 如果需要高德地图，确保配置了 AMAP_API_KEY 环境变量
            # amapServer:
            #   url: https://mcp.amap.com
            #   sse-endpoint: /sse?key=${AMAP_API_KEY}
        request-timeout: 60000

# 添加调试日志
logging:
  level:
    org.springframework.ai.mcp: DEBUG
    io.modelcontextprotocol: DEBUG
```

### 步骤 4: 添加 WebFlux 依赖（如果缺失）

**检查 Mcp-Server 的 pom.xml 是否包含 Spring WebFlux:**

如果没有，添加：
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webflux</artifactId>
</dependency>
```

## 🚀 启动步骤

### 1. 清理并重新编译

```powershell
# 在项目根目录
cd d:\university\JAVA\Explore-Study

# 清理并编译 Mcp-Server
cd Mcp-Server
mvn clean install

# 清理并编译 Dee
cd ..\Dee
mvn clean install
```

### 2. 启动 Mcp-Server

```powershell
cd d:\university\JAVA\Explore-Study\Mcp-Server
mvn spring-boot:run
```

**等待看到以下日志:**
```
Started McpServerApplication in X.XXX seconds
Netty started on port 8085
MCP Server enabled
```

### 3. 测试 Mcp-Server

**打开新的 PowerShell 窗口:**
```powershell
# 运行测试脚本
cd d:\university\JAVA\Explore-Study
.\test-mcp-connection.ps1
```

或手动测试：
```powershell
# 测试健康检查
curl http://localhost:8085/health

# 测试 MCP 状态
curl http://localhost:8085/mcp/status
```

### 4. 启动 Dee

**确认 Mcp-Server 正常运行后:**
```powershell
cd d:\university\JAVA\Explore-Study\Dee
mvn spring-boot:run
```

**查看日志中的 MCP 连接信息:**
```
Initializing MCP clients
Connected to MCP server: http://localhost:8085/mcp/book
Registered MCP tools: [getBookInfo, getBookStoreInfo, getBookRentInfo]
```

## 🔍 验证连接成功

### 方法 1: 查看 Dee 启动日志

在 Dee 的启动日志中搜索：
- `MCP` 关键字
- `Connected to MCP server`
- `Registered X MCP tools`

### 方法 2: 调用 MCP 工具测试

**使用 Postman 或 curl 测试:**
```powershell
# 测试聊天接口，让 AI 使用 MCP 工具
curl -X GET "http://localhost:6363/chat/tool?message=帮我查询《Spring实战》这本书的信息&userToken=test-token"
```

AI 应该会调用 `getBookInfo` 工具并返回书籍信息。

### 方法 3: 查看 MCP 服务器列表

```powershell
curl http://localhost:6363/mcp/server-list
```

## 📋 检查清单

完成以下检查确保配置正确：

- [ ] Mcp-Server pom.xml 使用 `spring-ai-starter-mcp-server-webflux`
- [ ] Dee pom.xml 使用 `spring-ai-starter-mcp-client-webflux`
- [ ] Mcp-Server 在 8085 端口成功启动
- [ ] 可以访问 `http://localhost:8085/health`
- [ ] 可以访问 `http://localhost:8085/mcp/status`
- [ ] Dee 启动日志显示 MCP 连接成功
- [ ] 日志级别设置为 DEBUG
- [ ] 两个服务的 enabled 都设置为 true

## ⚠️ 常见问题

### Q1: 修改 pom.xml 后编译失败
**A**: 运行 `mvn clean install -U` 强制更新依赖

### Q2: 端口 8085 被占用
**A**: 
```powershell
# 查找占用进程
netstat -ano | findstr :8085
# 结束进程（替换 PID）
taskkill /PID <PID> /F
```

### Q3: Dee 启动时提示找不到 MCP 服务器
**A**: 
1. 确保 Mcp-Server 先启动
2. 检查防火墙设置
3. 验证 URL 配置正确

### Q4: 日志中没有 MCP 相关信息
**A**: 
1. 检查 `enabled: true` 配置
2. 确认配置文件被正确加载（检查 spring.config.import）
3. 增加日志级别到 DEBUG

## 🎉 成功标志

当看到以下信息时，说明 MCP 连接成功：

**Mcp-Server 日志:**
```
MCP Server started successfully
SSE endpoint available at: /mcp/book
Registered tools: [getBookInfo, getBookStoreInfo, getBookRentInfo]
```

**Dee 日志:**
```
Initializing MCP clients
Connected to MCP server: http://localhost:8085/mcp/book
Successfully registered MCP tool callbacks
共注册了 1 个MCP工具回调
```

## 📞 需要帮助？

如果按照以上步骤仍然无法连接，请提供：
1. Mcp-Server 的完整启动日志
2. Dee 的完整启动日志
3. `test-mcp-connection.ps1` 的输出结果
