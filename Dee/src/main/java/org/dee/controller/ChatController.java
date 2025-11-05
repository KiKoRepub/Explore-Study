package org.dee.controller;

import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.dee.entity.ChatRecord;
import org.dee.entity.ChatRecordZip;
import org.dee.enums.PersistenceType;
import org.dee.service.*;
import org.dee.service.impl.ToolServiceImpl;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
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
    SSEService sseService;

    @GetMapping("/push")
    @ApiOperation(value = "使用记忆与模型聊天", notes = "向聊天模型发送消息，并根据聊天内存检索响应")
    public String chat(@RequestParam(value = "message") String message,
                       @RequestParam(value = "conversationId", required = false) String conversationId,
                       @RequestParam(value = "expireSeconds", required = false, defaultValue = "3600") long expireSeconds) {

        if (conversationId == null) conversationId = UUID.randomUUID().toString();


        System.out.println("——————————————————————————" + conversationId);

        return chattingService.chatWithCache(message, conversationId, expireSeconds);

    }

    @GetMapping("/push/stream")
    @ApiOperation(value = "使用记忆与模型聊天（SSE流式）", notes = "向聊天模型发送消息，通过SSE流式返回响应")
    public Map<String, String> chatStream(@RequestParam(value = "message") String message,
                                          @RequestParam(value = "conversationId", required = false) String conversationId,
                                          @RequestParam(value = "userId") String userId,
                                          @RequestParam(value = "expireSeconds", required = false, defaultValue = "3600") long expireSeconds) {

        if (conversationId == null) conversationId = UUID.randomUUID().toString();
        
        return chattingService.streamChatWithCache(message, conversationId, userId, expireSeconds);
    }

    @GetMapping("/tool")
    @ApiOperation(value = "使用工具与模型聊天", notes = "向聊天模型发送消息，并使用工具进行辅助")
    public String chatWithTool(@RequestParam(value = "message") String message,
                               @RequestParam(value = "conversationId", required = false) String conversationId,
                               @RequestParam(value = "expireSeconds", required = false, defaultValue = "3600") long expireSeconds) {

        if (conversationId == null) conversationId = UUID.randomUUID().toString();
        System.out.println("——————————————————————————" + conversationId);

        return chattingService.chatUsingTool(message, conversationId, expireSeconds);
    }

    @GetMapping("/tool/stream")
    @ApiOperation(value = "使用工具与模型聊天（SSE流式）", notes = "向聊天模型发送消息，使用工具辅助，通过SSE流式返回响应")
    public Map<String, String> chatWithToolStream(@RequestParam(value = "message") String message,
                                                   @RequestParam(value = "conversationId", required = false) String conversationId,
                                                   @RequestParam(value = "userId") String userId,
                                                   @RequestParam(value = "expireSeconds", required = false, defaultValue = "3600") long expireSeconds) {

        if (conversationId == null) conversationId = UUID.randomUUID().toString();
        
        return chattingService.streamChatUsingTool(message, conversationId, userId, expireSeconds);
    }

    /**
     * 手动触发持久化
     *
     * @param conversationId 对话ID
     * @return 执行结果
     */
    @PostMapping("/persist")
    @ApiOperation(value = "手动持久化对话", notes = "立即将 Redis 中的对话记录持久化到数据库")
    public String persistConversation(@RequestParam("conversationId") String conversationId) {
        try {

            chattingService.persistChatMessages(conversationId, PersistenceType.MANUAL);
            return "持久化成功";
        } catch (Exception e) {
            e.printStackTrace();
            return "持久化失败: " + e.getMessage();
        }
    }




}