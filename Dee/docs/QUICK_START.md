# 工具管理系统快速开始指南

## 🚀 快速开始

### 第一步：创建数据库表

在你的 MySQL 数据库中执行以下 SQL：

```sql
CREATE TABLE IF NOT EXISTS `tool` (
    `id` INT AUTO_INCREMENT PRIMARY KEY COMMENT '工具ID',
    `tool_name` VARCHAR(100) NOT NULL COMMENT '工具名称',
    `description` VARCHAR(500) COMMENT '工具描述',
    `class_name` VARCHAR(255) NOT NULL COMMENT '工具类的完整类名',
    `method_name` VARCHAR(100) NOT NULL COMMENT '工具方法名',
    `parameters` TEXT COMMENT '工具参数定义',
    `enabled` TINYINT DEFAULT 1 COMMENT '是否启用: 1-启用, 0-禁用',
    `category` VARCHAR(50) COMMENT '工具分类',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_enabled` (`enabled`),
    INDEX `idx_category` (`category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工具表';
```

或者直接执行：`src/main/resources/sql/tool.sql`

### 第二步：启动应用

```bash
mvn spring-boot:run
```

或者在 IDE 中运行 `DeeApplication` 主类。

### 第三步：加载工具到数据库

使用 Postman、curl 或浏览器访问：

```bash
curl -X POST http://localhost:8080/tool/load
```

**响应示例**：
```json
{
    "success": true,
    "message": "成功加载 3 个工具到数据库",
    "count": 3
}
```

### 第四步：查看已加载的工具

```bash
curl -X GET http://localhost:8080/tool/list
```

**响应示例**：
```json
{
    "success": true,
    "data": [
        {
            "id": 1,
            "toolName": "generateImage",
            "description": "图像处理工具",
            "className": "org.dee.tools.ImageTool",
            "methodName": "generateImage",
            "parameters": "String",
            "enabled": 1,
            "category": "Image",
            "createdAt": "2025-11-03T17:00:00",
            "updatedAt": "2025-11-03T17:00:00"
        },
        {
            "id": 2,
            "toolName": "getCurrentWeather",
            "description": "天气查询工具",
            "className": "org.dee.tools.WeatherTool",
            "methodName": "getCurrentWeather",
            "parameters": "String",
            "enabled": 1,
            "category": "Weather",
            "createdAt": "2025-11-03T17:00:00",
            "updatedAt": "2025-11-03T17:00:00"
        }
    ],
    "count": 2
}
```

## 📝 常用操作

### 查看启用的工具

```bash
curl -X GET http://localhost:8080/tool/enabled
```

### 禁用某个工具

```bash
# 禁用 ID 为 1 的工具
curl -X PUT http://localhost:8080/tool/1/toggle
```

### 重新启用工具

```bash
# 再次调用即可重新启用
curl -X PUT http://localhost:8080/tool/1/toggle
```

### 删除工具

```bash
# 删除 ID 为 1 的工具
curl -X DELETE http://localhost:8080/tool/1
```

## 🛠️ 创建自定义工具

### 1. 创建工具类

在 `src/main/java/org/dee/tools/` 目录下创建新的工具类：

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
    
    @Tool(description = "计算两个数的乘积")
    public double multiply(double a, double b) {
        return a * b;
    }
}
```

### 2. 重启应用并加载

```bash
# 重启应用后，再次调用加载接口
curl -X POST http://localhost:8080/tool/load
```

新工具会自动被扫描并保存到数据库。

## 📦 使用 Postman

导入 Postman 集合文件：

1. 打开 Postman
2. 点击 Import
3. 选择 `docs/Tool_Management_API.postman_collection.json`
4. 开始测试 API

## 🔍 Swagger 文档

如果项目已配置 Swagger，可以访问：

```
http://localhost:8080/swagger-ui.html
```

在 "工具管理" 分组下查看所有 API 接口。

## ⚠️ 注意事项

1. **首次加载**：建议在首次部署时调用一次 `/tool/load` 接口
2. **重复加载**：多次调用会导致重复数据，建议先清空表或添加去重逻辑
3. **工具注解**：确保工具类使用 `@MyTool` 注解，方法使用 `@Tool` 注解
4. **数据库配置**：检查 `application.properties` 中的数据库连接配置

## 🎯 下一步

- 查看完整文档：`docs/TOOL_MANAGEMENT.md`
- 了解工具使用场景        
- 创建更多自定义工具
- 集成到聊天系统中

## 💡 示例场景

### 场景 1：在聊天中使用工具

```java
@GetMapping("/chat/with-tools")
public String chatWithTools(@RequestParam String message) {
    // 从数据库加载启用的工具
    List<Tool> enabledTools = toolService.loadEnabledToolsFromDatabase();
    
    // 将工具转换为 ToolCallback 并用于聊天
    // ... 实现聊天逻辑
    
    return "响应内容";
}
```

### 场景 2：动态管理工具

管理员可以通过 API 动态启用/禁用工具，无需重启应用：

```bash
# 禁用天气工具
curl -X PUT http://localhost:8080/tool/2/toggle

# 查看当前启用的工具
curl -X GET http://localhost:8080/tool/enabled
```

## 🤝 需要帮助？

如有问题，请查看：
- 完整文档：`docs/TOOL_MANAGEMENT.md`
- 代码示例：`src/main/java/org/dee/tools/`
- API 接口：`src/main/java/org/dee/controller/ToolController.java`
