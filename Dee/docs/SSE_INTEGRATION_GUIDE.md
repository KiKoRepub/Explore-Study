# SSE 集成指南

## 概述

本文档说明如何使用 SSE (Server-Sent Events) 与 AI 对话接口进行实时通信。

## 架构说明

### 核心组件

1. **SSEServer** - SSE 连接管理器
   - 管理所有用户的 SSE 连接
   - 提供消息发送、连接管理等功能

2. **SSEController** - SSE 连接控制器
   - `/sse/connect` - 建立 SSE 连接
   - `/sse/disconnect` - 断开连接
   - `/sse/status` - 检查连接状态
   - `/sse/count` - 获取连接数

3. **ChatController** - AI 对话控制器
   - `/chat/push/stream` - 流式对话（带记忆）
   - `/chat/tool/stream` - 流式对话（带工具）

4. **SSEResponseHelper** - SSE 响应工具类
   - 统一处理流式响应
   - 封装消息发送逻辑

## 使用流程

### 1. 建立 SSE 连接

前端首先需要建立 SSE 连接：

```javascript
// 使用 EventSource 建立 SSE 连接
const userId = 'user123'; // 用户唯一标识
const eventSource = new EventSource(`http://localhost:8080/sse/connect?userId=${userId}`);

// 监听连接成功事件
eventSource.addEventListener('connected', (event) => {
    console.log('SSE 连接成功:', event.data);
});

// 监听消息块事件
eventSource.addEventListener('message', (event) => {
    const data = JSON.parse(event.data);
    console.log('收到消息:', data);
    
    if (data.type === 'chunk') {
        // 处理消息块（逐字显示）
        appendToChat(data.content);
    } else if (data.type === 'complete') {
        // 对话完成
        console.log('完整响应:', data.fullResponse);
        markChatComplete();
    }
});

// 监听错误事件
eventSource.addEventListener('error', (event) => {
    const data = JSON.parse(event.data);
    console.error('发生错误:', data.message);
    showError(data.message);
});

// 监听连接错误
eventSource.onerror = (error) => {
    console.error('SSE 连接错误:', error);
    eventSource.close();
};
```

### 2. 发送对话请求

建立连接后，可以发送对话请求：

```javascript
// 发送普通对话请求
async function sendMessage(message) {
    const response = await fetch(`http://localhost:8080/chat/push/stream?message=${encodeURIComponent(message)}&userId=${userId}`, {
        method: 'GET'
    });
    
    const result = await response.json();
    console.log('对话已开始:', result);
    // result: { status: "processing", conversationId: "xxx", message: "..." }
}

// 发送带工具的对话请求
async function sendMessageWithTools(message) {
    const response = await fetch(`http://localhost:8080/chat/tool/stream?message=${encodeURIComponent(message)}&userId=${userId}`, {
        method: 'GET'
    });
    
    const result = await response.json();
    console.log('工具对话已开始:', result);
}
```

### 3. 处理响应消息

SSE 会推送以下类型的消息：

#### 消息块 (chunk)
```json
{
    "type": "chunk",
    "content": "这是一段",
    "conversationId": "xxx",
    "timestamp": 1234567890
}
```

#### 完成消息 (complete)
```json
{
    "type": "complete",
    "conversationId": "xxx",
    "fullResponse": "这是完整的响应内容",
    "timestamp": 1234567890
}
```

#### 错误消息 (error)
```json
{
    "type": "error",
    "message": "错误信息",
    "conversationId": "xxx",
    "timestamp": 1234567890
}
```

### 4. 断开连接

使用完毕后，记得断开连接：

```javascript
// 关闭 EventSource
eventSource.close();

// 可选：调用服务器断开接口
await fetch(`http://localhost:8080/sse/disconnect?userId=${userId}`, {
    method: 'DELETE'
});
```

## 完整前端示例

```html
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <title>AI 对话 - SSE 示例</title>
    <style>
        #chat-container {
            width: 600px;
            height: 400px;
            border: 1px solid #ccc;
            overflow-y: auto;
            padding: 10px;
            margin-bottom: 10px;
        }
        .message {
            margin: 5px 0;
            padding: 5px;
        }
        .user-message {
            background-color: #e3f2fd;
            text-align: right;
        }
        .bot-message {
            background-color: #f5f5f5;
        }
        .error-message {
            background-color: #ffebee;
            color: #c62828;
        }
    </style>
</head>
<body>
    <h1>AI 对话 - SSE 实时通信</h1>
    
    <div id="chat-container"></div>
    
    <input type="text" id="message-input" placeholder="输入消息..." style="width: 500px;">
    <button onclick="sendMessage()">发送</button>
    <button onclick="sendMessageWithTools()">发送（带工具）</button>
    <button onclick="disconnect()">断开连接</button>
    
    <script>
        const userId = 'user_' + Date.now();
        let eventSource = null;
        let conversationId = null;
        let currentBotMessage = null;
        
        // 初始化 SSE 连接
        function initSSE() {
            eventSource = new EventSource(`http://localhost:8080/sse/connect?userId=${userId}`);
            
            eventSource.addEventListener('connected', (event) => {
                console.log('SSE 连接成功:', event.data);
                addSystemMessage('已连接到服务器');
            });
            
            eventSource.addEventListener('message', (event) => {
                const data = JSON.parse(event.data);
                handleMessage(data);
            });
            
            eventSource.addEventListener('error', (event) => {
                const data = JSON.parse(event.data);
                addErrorMessage(data.message);
            });
            
            eventSource.onerror = (error) => {
                console.error('SSE 连接错误:', error);
                addSystemMessage('连接已断开');
            };
        }
        
        // 处理消息
        function handleMessage(data) {
            switch(data.type) {
                case 'chunk':
                    if (!currentBotMessage) {
                        currentBotMessage = addBotMessage('');
                    }
                    currentBotMessage.textContent += data.content;
                    break;
                    
                case 'complete':
                    console.log('对话完成:', data.fullResponse);
                    currentBotMessage = null;
                    break;
                    
                case 'error':
                    addErrorMessage(data.message);
                    currentBotMessage = null;
                    break;
            }
        }
        
        // 发送消息
        async function sendMessage() {
            const input = document.getElementById('message-input');
            const message = input.value.trim();
            
            if (!message) return;
            
            addUserMessage(message);
            input.value = '';
            
            try {
                const response = await fetch(
                    `http://localhost:8080/chat/push/stream?message=${encodeURIComponent(message)}&userId=${userId}`,
                    { method: 'GET' }
                );
                
                const result = await response.json();
                
                if (result.error) {
                    addErrorMessage(result.error);
                } else {
                    conversationId = result.conversationId;
                    console.log('对话已开始:', result);
                }
            } catch (error) {
                addErrorMessage('发送失败: ' + error.message);
            }
        }
        
        // 发送带工具的消息
        async function sendMessageWithTools() {
            const input = document.getElementById('message-input');
            const message = input.value.trim();
            
            if (!message) return;
            
            addUserMessage(message);
            input.value = '';
            
            try {
                const response = await fetch(
                    `http://localhost:8080/chat/tool/stream?message=${encodeURIComponent(message)}&userId=${userId}`,
                    { method: 'GET' }
                );
                
                const result = await response.json();
                
                if (result.error) {
                    addErrorMessage(result.error);
                } else {
                    conversationId = result.conversationId;
                    console.log('工具对话已开始:', result);
                }
            } catch (error) {
                addErrorMessage('发送失败: ' + error.message);
            }
        }
        
        // 断开连接
        function disconnect() {
            if (eventSource) {
                eventSource.close();
                addSystemMessage('已断开连接');
            }
        }
        
        // UI 辅助函数
        function addUserMessage(text) {
            const div = document.createElement('div');
            div.className = 'message user-message';
            div.textContent = '你: ' + text;
            document.getElementById('chat-container').appendChild(div);
            scrollToBottom();
        }
        
        function addBotMessage(text) {
            const div = document.createElement('div');
            div.className = 'message bot-message';
            div.textContent = 'AI: ' + text;
            document.getElementById('chat-container').appendChild(div);
            scrollToBottom();
            return div;
        }
        
        function addErrorMessage(text) {
            const div = document.createElement('div');
            div.className = 'message error-message';
            div.textContent = '错误: ' + text;
            document.getElementById('chat-container').appendChild(div);
            scrollToBottom();
        }
        
        function addSystemMessage(text) {
            const div = document.createElement('div');
            div.className = 'message';
            div.style.color = '#666';
            div.textContent = '[系统] ' + text;
            document.getElementById('chat-container').appendChild(div);
            scrollToBottom();
        }
        
        function scrollToBottom() {
            const container = document.getElementById('chat-container');
            container.scrollTop = container.scrollHeight;
        }
        
        // 页面加载时初始化
        window.onload = initSSE;
        
        // 页面关闭时断开连接
        window.onbeforeunload = disconnect;
        
        // 回车发送
        document.getElementById('message-input').addEventListener('keypress', (e) => {
            if (e.key === 'Enter') {
                sendMessage();
            }
        });
    </script>
</body>
</html>
```

## API 接口说明

### SSE 连接管理

#### 1. 建立连接
- **URL**: `GET /sse/connect`
- **参数**: `userId` (必需) - 用户唯一标识
- **返回**: `SseEmitter` 对象
- **说明**: 建立 SSE 长连接，用于接收实时消息

#### 2. 断开连接
- **URL**: `DELETE /sse/disconnect`
- **参数**: `userId` (必需)
- **返回**: 操作结果字符串

#### 3. 检查连接状态
- **URL**: `GET /sse/status`
- **参数**: `userId` (必需)
- **返回**: 连接状态字符串

#### 4. 获取连接数
- **URL**: `GET /sse/count`
- **返回**: 当前活跃连接数

### AI 对话接口

#### 1. 流式对话（带记忆）
- **URL**: `GET /chat/push/stream`
- **参数**:
  - `message` (必需) - 用户消息
  - `userId` (必需) - 用户ID
  - `conversationId` (可选) - 对话ID
  - `expireSeconds` (可选，默认3600) - 缓存过期时间
- **返回**: 
  ```json
  {
    "status": "processing",
    "conversationId": "xxx",
    "message": "对话处理中，请通过SSE接收响应"
  }
  ```

#### 2. 流式对话（带工具）
- **URL**: `GET /chat/tool/stream`
- **参数**: 同上
- **返回**: 同上

## 注意事项

1. **连接管理**
   - 使用前必须先建立 SSE 连接
   - 每个 userId 只能有一个活跃连接
   - 新连接会自动替换旧连接

2. **消息格式**
   - 所有 SSE 消息都是 JSON 格式
   - 需要使用 `JSON.parse()` 解析

3. **错误处理**
   - 监听 `error` 事件处理业务错误
   - 监听 `onerror` 处理连接错误

4. **性能优化**
   - 对话处理在异步线程中执行
   - 不会阻塞主线程

5. **浏览器兼容性**
   - 现代浏览器都支持 EventSource
   - IE 不支持，需要使用 polyfill

## 故障排查

### 问题：无法建立连接
- 检查服务器是否运行
- 检查 CORS 配置
- 检查 userId 是否正确

### 问题：收不到消息
- 确认已建立 SSE 连接
- 检查 userId 是否匹配
- 查看服务器日志

### 问题：连接频繁断开
- 检查网络稳定性
- 检查服务器资源
- 考虑添加重连机制

## 扩展功能

可以基于现有架构扩展以下功能：

1. **自动重连** - 连接断开后自动重新连接
2. **消息队列** - 缓存离线消息
3. **多会话管理** - 支持多个对话同时进行
4. **消息历史** - 保存和加载历史消息
5. **打字指示器** - 显示 AI 正在输入
6. **消息撤回** - 取消正在进行的对话

## 总结

通过 SSE 集成，AI 对话接口可以实现：
- ✅ 实时流式响应
- ✅ 逐字显示效果
- ✅ 更好的用户体验
- ✅ 降低前端轮询压力
- ✅ 支持长时间对话
