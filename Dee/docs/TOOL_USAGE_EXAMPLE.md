# 工具使用示例文档

## 概述

本文档展示如何在 ChatController 中使用数据库管理的工具。

## 实现原理

### 工作流程

```
1. 开发者创建工具类 (@MyTool + @Tool)
   ↓
2. Spring AI 自动扫描并注册 ToolCallback
   ↓
3. 工具信息保存到数据库 (调用 /tool/load)
   ↓
4. DynamicToolCallbackFilter 根据数据库状态过滤工具
   ↓
5. ChatClient 使用过滤后的工具进行对话
```

### 核心组件

- **SQLTool**: 数据库实体，存储工具元数据
- **ToolService**: 工具管理服务
- **DynamicToolCallbackFilter**: 动态工具过滤器
- **ToolCallback**: Spring AI 的工具回调接口

## 使用示例

### 1. 在 ChatController 中使用启用的工具

```java
@RestController
@RequestMapping("/chat")
public class ChatController {

    @Autowired
    private ChatClient chatClient;

    @Autowired
    private ToolService toolService;

    /**
     * 使用数据库中启用的工具进行对话
     */
    @GetMapping("/with-enabled-tools")
    public String chatWithEnabledTools(@RequestParam String message) {
        // 获取启用的工具
        ToolCallback[] enabledTools = toolService.getEnabledToolCallbacks();
        
        // 使用工具进行对话
        ChatResponse response = chatClient.prompt()
                .user(message)
                .tools(enabledTools)  // 方式 1: 使用 tools()
                .call()
                .chatResponse();
        
        return response.getResult().getOutput().getText();
    }

    /**
     * 使用 toolCallbacks 方式
     */
    @GetMapping("/with-tool-callbacks")
    public String chatWithToolCallbacks(@RequestParam String message) {
        ToolCallback[] enabledTools = toolService.getEnabledToolCallbacks();
        
        ChatResponse response = chatClient.prompt()
                .user(message)
                .toolCallbacks(enabledTools)  // 方式 2: 使用 toolCallbacks()
                .call()
                .chatResponse();
        
        return response.getResult().getOutput().getText();
    }
}
```

### 2. 使用特定分类的工具

```java
@Autowired
private DynamicToolCallbackFilter toolCallbackFilter;

@GetMapping("/with-weather-tools")
public String chatWithWeatherTools(@RequestParam String message) {
    // 只使用天气相关的工具
    ToolCallback[] weatherTools = toolCallbackFilter.getToolCallbacksByCategory("Weather");
    
    ChatResponse response = chatClient.prompt()
            .user(message)
            .tools(weatherTools)
            .call()
            .chatResponse();
    
    return response.getResult().getOutput().getText();
}
```

### 3. 动态切换工具

```java
@GetMapping("/chat-dynamic")
public String chatDynamic(
        @RequestParam String message,
        @RequestParam(required = false) String category) {
    
    ToolCallback[] tools;
    
    if (category != null) {
        // 使用指定分类的工具
        tools = toolCallbackFilter.getToolCallbacksByCategory(category);
    } else {
        // 使用所有启用的工具
        tools = toolService.getEnabledToolCallbacks();
    }
    
    ChatResponse response = chatClient.prompt()
            .user(message)
            .tools(tools)
            .call()
            .chatResponse();
    
    return response.getResult().getOutput().getText();
}
```

## 完整工作流程示例

### 步骤 1: 创建工具类

```java
package org.dee.tools;

import org.dee.annotions.MyTool;
import org.springframework.ai.tool.annotation.Tool;

@MyTool("计算器工具")
public class CalculatorTool {
    
    @Tool(description = "计算两个数的和")
    public double add(double a, double b) {
        return a + b;
    }
    
    @Tool(description = "计算两个数的差")
    public double subtract(double a, double b) {
        return a - b;
    }
}
```

### 步骤 2: 启动应用并加载工具

```bash
# 启动应用
mvn spring-boot:run

# 加载工具到数据库
curl -X POST http://localhost:8080/tool/load
```

### 步骤 3: 查看已加载的工具

```bash
curl -X GET http://localhost:8080/tool/list
```

响应：
```json
{
    "code": 200,
    "message": "success",
    "data": [
        {
            "id": 1,
            "toolName": "add",
            "description": "计算两个数的和",
            "className": "org.dee.tools.CalculatorTool",
            "methodName": "add",
            "parameters": "double, double",
            "enabled": 1,
            "category": "Calculator"
        },
        {
            "id": 2,
            "toolName": "subtract",
            "description": "计算两个数的差",
            "className": "org.dee.tools.CalculatorTool",
            "methodName": "subtract",
            "parameters": "double, double",
            "enabled": 1,
            "category": "Calculator"
        }
    ]
}
```

### 步骤 4: 使用工具进行对话

```bash
curl -X GET "http://localhost:8080/chat/with-enabled-tools?message=帮我计算 123 加 456"
```

响应：
```
根据计算，123 加 456 等于 579。
```

### 步骤 5: 动态管理工具

```bash
# 禁用减法工具
curl -X PUT http://localhost:8080/tool/2/toggle

# 再次对话，减法工具将不可用
curl -X GET "http://localhost:8080/chat/with-enabled-tools?message=帮我计算 100 减 50"
```

## 高级用法

### 1. 缓存工具列表

为了提高性能，可以缓存工具列表：

```java
@Service
public class CachedToolService {
    
    @Autowired
    private ToolService toolService;
    
    private ToolCallback[] cachedTools;
    private long lastUpdateTime;
    private static final long CACHE_DURATION = 60000; // 1分钟
    
    public ToolCallback[] getEnabledTools() {
        long now = System.currentTimeMillis();
        if (cachedTools == null || (now - lastUpdateTime) > CACHE_DURATION) {
            cachedTools = toolService.getEnabledToolCallbacks();
            lastUpdateTime = now;
        }
        return cachedTools;
    }
    
    public void refreshCache() {
        cachedTools = toolService.getEnabledToolCallbacks();
        lastUpdateTime = System.currentTimeMillis();
    }
}
```

### 2. 工具使用日志

记录工具的使用情况：

```java
@Aspect
@Component
public class ToolUsageLogger {
    
    @Around("execution(* org.dee.tools..*.*(..))")
    public Object logToolUsage(ProceedingJoinPoint joinPoint) throws Throwable {
        String toolName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();
        
        System.out.println("工具调用: " + toolName + ", 参数: " + Arrays.toString(args));
        
        long startTime = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        long duration = System.currentTimeMillis() - startTime;
        
        System.out.println("工具执行完成: " + toolName + ", 耗时: " + duration + "ms");
        
        return result;
    }
}
```

### 3. 条件工具加载

根据用户权限或其他条件加载工具：

```java
public ToolCallback[] getToolsForUser(String userId) {
    // 获取用户权限
    UserPermission permission = userService.getPermission(userId);
    
    // 获取所有启用的工具
    List<SQLTool> enabledTools = toolService.loadEnabledToolsFromDatabase();
    
    // 根据权限过滤工具
    List<SQLTool> allowedTools = enabledTools.stream()
            .filter(tool -> permission.canUseTool(tool.getCategory()))
            .collect(Collectors.toList());
    
    // 转换为 ToolCallback
    return toolService.convertToToolCallbacks(allowedTools);
}
```

## 注意事项

1. **工具名称匹配**：DynamicToolCallbackFilter 通过 Bean 名称匹配工具，确保方法名唯一
2. **性能考虑**：频繁查询数据库会影响性能，建议使用缓存
3. **工具更新**：修改工具代码后需要重启应用并重新加载到数据库
4. **错误处理**：工具方法应该有适当的异常处理，避免影响对话流程

## 故障排查

### 问题 1: 工具未被识别

**症状**：调用 `/tool/load` 后，工具列表为空

**解决方案**：
- 检查工具类是否使用了 `@MyTool` 注解
- 检查工具方法是否使用了 `@Tool` 注解
- 确保工具类在 Spring 扫描路径下

### 问题 2: 工具调用失败

**症状**：ChatClient 无法调用工具

**解决方案**：
- 检查工具是否在数据库中启用
- 查看控制台日志，确认工具是否被加载
- 验证工具方法的参数类型是否正确

### 问题 3: 工具状态不同步

**症状**：修改数据库后，工具状态未更新

**解决方案**：
- 清除缓存（如果使用了缓存）
- 重新调用 `getEnabledToolCallbacks()`
- 考虑实现自动刷新机制

## 下一步

- 实现工具使用统计
- 添加工具版本管理
- 创建工具测试框架
- 实现工具热加载
