package org.dee.controller;

import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.dee.enums.PersistenceType;
import org.dee.service.*;
import org.dee.entity.vo.ResultBean;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/chat")
public class ChatController {

    @Autowired
    ChatClient chatClient;

    @Autowired
    ChattingService chattingService;
    @Autowired
    UserService userService;

    @Autowired
    SSEService sseService;

    @GetMapping("/push")
    @ApiOperation(value = "使用记忆与模型聊天", notes = "向聊天模型发送消息，并根据聊天内存检索响应")
    public ResultBean chat(@RequestParam(value = "message") String message,
                       @RequestParam(value = "conversationId", required = false) String conversationId,
                       @RequestParam(value = "userToken") String userToken,
                       @RequestParam(value = "expireSeconds", required = false, defaultValue = "3600") long expireSeconds) {

        if (conversationId == null) conversationId = UUID.randomUUID().toString();

        String userId = userService.analyzeUserIdFromToken(userToken);
        System.out.println("——————————————————————————" + conversationId);
        return chattingService.chatWithCache(message, conversationId, userId, expireSeconds);

    }

    @GetMapping("/push/stream")
    @ApiOperation(value = "使用记忆与模型聊天（SSE流式）", notes = "向聊天模型发送消息，通过SSE流式返回响应")
    public ResultBean chatStream(@RequestParam(value = "message") String message,
                                 @RequestParam(value = "conversationId", required = false) String conversationId,
                                 @RequestParam(value = "userToken") String userToken,
                                 @RequestParam(value = "expireSeconds", required = false, defaultValue = "3600") long expireSeconds) {

        if (conversationId == null) conversationId = UUID.randomUUID().toString();
        String userId = userService.analyzeUserIdFromToken(userToken);
        return chattingService.streamChatWithCache(message, conversationId, userId, expireSeconds);
    }

    @GetMapping("/tool")
    @ApiOperation(value = "使用工具与模型聊天", notes = "向聊天模型发送消息，并使用工具进行辅助")
    public ResultBean chatWithTool(@RequestParam(value = "message") String message,
                               @RequestParam(value = "conversationId", required = false) String conversationId,
                               @RequestParam(value = "userToken") String userToken,
                               @RequestParam(value = "expireSeconds", required = false, defaultValue = "3600") long expireSeconds) {

        if (conversationId == null) conversationId = UUID.randomUUID().toString();
        String userId = userService.analyzeUserIdFromToken(userToken);
        System.out.println("——————————————————————————" + conversationId);

        return chattingService.chatUsingTool(message, conversationId, userId, expireSeconds);
    }

    @GetMapping("/tool/stream")
    @ApiOperation(value = "使用工具与模型聊天（SSE流式）", notes = "向聊天模型发送消息，使用工具辅助，通过SSE流式返回响应")
    public ResultBean chatWithToolStream(@RequestParam(value = "message") String message,
                                                   @RequestParam(value = "conversationId", required = false) String conversationId,
                                                   @RequestParam(value = "userToken") String userToken,
                                                   @RequestParam(value = "expireSeconds", required = false, defaultValue = "3600") long expireSeconds) {

        if (conversationId == null) conversationId = UUID.randomUUID().toString();

        String userId = userService.analyzeUserIdFromToken(userToken);

        return chattingService.streamChatUsingTool(message, conversationId, userId, expireSeconds);
    }

    @GetMapping("/mcp")
    @ApiOperation(value = "使用MCP工具与模型聊天", notes = "向聊天模型发送消息，并使用MCP工具进行辅助")
    public ResultBean chatWithMcpTool(@RequestParam(value = "message") String message,
                               @RequestParam(value = "conversationId", required = false) String conversationId,
                               @RequestParam(value = "userToken") String userToken,
                               @RequestParam(value = "expireSeconds", required = false, defaultValue = "3600") long expireSeconds,
                               @RequestParam(value = "mcpNames", required = false) List<String> mcpNames) {

        if (conversationId == null) conversationId = UUID.randomUUID().toString();
        String userId = userService.analyzeUserIdFromToken(userToken);
        System.out.println("——————————————————————————" + conversationId);

        return chattingService.chatUsingMcpTool(message, conversationId, userId, expireSeconds, mcpNames);
    }


    @GetMapping("/mcp/stream")
    @ApiOperation(value = "使用MCP工具与模型聊天（SSE流式）", notes = "向聊天模型发送消息，使用MCP工具辅助，通过SSE流式返回响应")
    public ResultBean chatWithMcpToolStream(@RequestParam(value = "message") String message,
                                                   @RequestParam(value = "conversationId", required = false) String conversationId,
                                                   @RequestParam(value = "userToken") String userToken,
                                                   @RequestParam(value = "expireSeconds", required = false, defaultValue = "3600") long expireSeconds,
                                                   @RequestParam(value = "mcpNames", required = false) List<String> mcpNames) {

        if (conversationId == null) conversationId = UUID.randomUUID().toString();

        String userId = userService.analyzeUserIdFromToken(userToken);

        return chattingService.streamChatUsingMcpTool(message, conversationId, userId, expireSeconds, mcpNames);
    }
    /**
     * 手动触发持久化
     *
     * @param conversationId 对话ID
     * @return 执行结果
     */
    @PostMapping("/persist")
    @ApiOperation(value = "手动持久化对话", notes = "立即将 Redis 中的对话记录持久化到数据库")
    public String persistConversation(@RequestParam("conversationId") String conversationId,
                                      @RequestParam("userToken") String userToken) {
        try {
            String userId = userService.analyzeUserIdFromToken(userToken);
            chattingService.persistChatMessages(conversationId, userId, PersistenceType.MANUAL);
            return "持久化成功";
        } catch (Exception e) {
            e.printStackTrace();
            return "持久化失败: " + e.getMessage();
        }
    }




}