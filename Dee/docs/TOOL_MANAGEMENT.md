# 工具管理系统使用文档

## 概述

工具管理系统允许你将应用中的工具（带有 `@MyTool` 和 `@Tool` 注解的类和方法）持久化到数据库中，并提供完整的 CRUD 操作。

## 数据库表结构

### tool 表

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | INT | 主键，自增 |
| tool_name | VARCHAR(100) | 工具名称（方法名） |
| description | VARCHAR(500) | 工具描述（来自 @MyTool 注解） |
| class_name | VARCHAR(255) | 工具类的完整类名 |
| method_name | VARCHAR(100) | 工具方法名 |
| parameters | TEXT | 工具参数定义 |
| enabled | TINYINT | 是否启用：1-启用，0-禁用 |
| category | VARCHAR(50) | 工具分类 |
| created_at | DATETIME | 创建时间 |
| updated_at | DATETIME | 更新时间 |

## 初始化步骤

### 1. 创建数据库表

执行 `src/main/resources/sql/tool.sql` 文件中的 SQL 语句：

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

### 2. 启动应用

确保应用正常启动，Spring 会自动扫描所有带 `@MyTool` 注解的类。

## API 接口说明

### 1. 加载工具到数据库

**接口**: `POST /tool/load`

**描述**: 扫描所有带 `@MyTool` 注解的类，将工具信息保存到数据库

**请求示例**:
```bash
curl -X POST http://localhost:8080/tool/load
```

**响应示例**:
```json
{
    "success": true,
    "message": "成功加载 3 个工具到数据库",
    "count": 3
}
```

### 2. 获取所有工具

**接口**: `GET /tool/list`

**描述**: 从数据库查询所有工具列表

**请求示例**:
```bash
curl -X GET http://localhost:8080/tool/list
```

**响应示例**:
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
        }
    ],
    "count": 1
}
```

### 3. 获取启用的工具

**接口**: `GET /tool/enabled`

**描述**: 从数据库查询所有启用状态的工具

**请求示例**:
```bash
curl -X GET http://localhost:8080/tool/enabled
```

**响应示例**:
```json
{
    "success": true,
    "data": [
        {
            "id": 1,
            "toolName": "generateImage",
            "enabled": 1,
            ...
        }
    ],
    "count": 1
}
```

### 4. 切换工具状态

**接口**: `PUT /tool/{id}/toggle`

**描述**: 启用或禁用指定的工具

**请求示例**:
```bash
curl -X PUT http://localhost:8080/tool/1/toggle
```

**响应示例**:
```json
{
    "success": true,
    "message": "工具状态切换成功"
}
```

### 5. 删除工具

**接口**: `DELETE /tool/{id}`

**描述**: 从数据库删除指定的工具

**请求示例**:
```bash
curl -X DELETE http://localhost:8080/tool/1
```

**响应示例**:
```json
{
    "success": true,
    "message": "工具删除成功"
}
```

## 代码使用示例

### 在 Service 中使用

```java
@Service
public class MyService {
    
    @Autowired
    private ToolService toolService;
    
    public void loadTools() {
        // 1. 将现有工具加载到数据库
        int count = toolService.loadExistingToolsToDatabase();
        System.out.println("加载了 " + count + " 个工具");
        
        // 2. 从数据库获取所有工具
        List<Tool> allTools = toolService.loadToolsFromDatabase();
        
        // 3. 从数据库获取启用的工具
        List<Tool> enabledTools = toolService.loadEnabledToolsFromDatabase();
        
        // 4. 切换工具状态
        toolService.toggleToolStatus(1);
        
        // 5. 删除工具
        toolService.deleteTool(1);
    }
}
```

## 创建新工具

### 1. 创建工具类

```java
package org.dee.tools;

import org.dee.annotions.MyTool;
import org.springframework.ai.tool.annotation.Tool;

@MyTool("天气查询工具")
public class WeatherTool {
    
    @Tool
    public String getWeather(String city) {
        // 实现天气查询逻辑
        return "北京今天晴天，温度 20°C";
    }
    
    @Tool
    public String getForecast(String city, int days) {
        // 实现天气预报逻辑
        return "未来 " + days + " 天的天气预报...";
    }
}
```

### 2. 加载到数据库

启动应用后，调用 `POST /tool/load` 接口，系统会自动扫描并保存新工具。

## 工作原理

1. **自动扫描**: 使用 Spring 的 `ApplicationContext` 获取所有带 `@MyTool` 注解的 Bean
2. **反射提取**: 通过反射获取类中带 `@Tool` 注解的方法信息
3. **批量保存**: 使用 MyBatis 的批量插入功能将工具信息保存到数据库
4. **状态管理**: 通过 `enabled` 字段控制工具的启用/禁用状态

## 注意事项

1. **注解要求**: 工具类必须使用 `@MyTool` 注解，工具方法必须使用 `@Tool` 注解
2. **重复加载**: 多次调用加载接口会导致重复数据，建议在首次部署时调用一次
3. **数据库连接**: 确保数据库连接配置正确
4. **权限控制**: 建议在生产环境中为工具管理接口添加权限验证

## 扩展功能

可以根据需要扩展以下功能：

- 工具版本管理
- 工具使用统计
- 工具参数验证
- 工具执行日志
- 工具权限控制
- 工具分类管理
