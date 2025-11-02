package org.dee.controller;

import io.swagger.annotations.ApiOperation;
import org.dee.entity.ChatRecord;
import org.dee.entity.ChatRecordZip;
import org.dee.service.ChatContextService;
import org.dee.service.ChatRecordService;
import org.dee.service.CacheChatService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/chat")
public class ChatController {

    @Autowired
    ChatClient chatClient;


    @Autowired
    ChatContextService chatContextService;

    @Autowired
    ChatRecordService chatRecordService;

    @Autowired
    CacheChatService cacheChatService;

    @GetMapping("/push")
    @ApiOperation(value = "使用记忆与模型聊天", notes = "向聊天模型发送消息，并根据聊天内存检索响应")
    public String chat(@RequestParam(value = "message") String message,
                       @RequestParam(value = "conversationId",required = false) String conversationId,
                       @RequestParam(value = "expireSeconds",required = false, defaultValue = "3600") long expireSeconds){

        if (conversationId == null) conversationId = UUID.randomUUID().toString();


        System.out.println("——————————————————————————"+conversationId);

        // 加载上下文：获取历史对话记录和概要
        String contextPrompt = buildContextPrompt(conversationId, message);


        ChatResponse response = chatClient.prompt()
                .user(contextPrompt)
                .call()
                .chatResponse();

        String botResponse = response.getResult().getOutput().getText();

        // 保存到 Redis，设置过期时间
        cacheChatService.cacheChatMessage(conversationId, message, botResponse, expireSeconds);

        return botResponse;
    }

//    @PostMapping("/record/save")
//    @ApiOperation(value = "保存聊天记录", notes = "将聊天记录持久化到数据库")
//    public boolean saveChatRecord(@RequestParam("conversationId") String conversationId,
//                                     @RequestParam("userMessage") String userMessage,
//                                     @RequestParam("botResponse") String botResponse) {
//        return chatRecordService.saveChatRecord(conversationId, userMessage, botResponse);
//    }


//    @PostMapping("/record/zip/save")
//    @ApiOperation(value = "保存对话概要", notes = "将对话概要持久化到数据库，如果已存在则更新")
//    public boolean saveChatRecordZip(@RequestParam("conversationId") String conversationId,
//                                           @RequestParam("title") String title,
//                                           @RequestParam("compressedData") String compressedData) {
//        return chatRecordService.saveChatRecordZip(conversationId, title, compressedData);
//    }

    /**
     * 手动触发持久化
     * @param conversationId 对话ID
     * @return 执行结果
     */
    @PostMapping("/persist")
    @ApiOperation(value = "手动持久化对话", notes = "立即将 Redis 中的对话记录持久化到数据库")
    public String persistConversation(@RequestParam("conversationId") String conversationId) {
        try {

            cacheChatService.persistChatMessages(conversationId);
            return "持久化成功";
        } catch (Exception e) {
            e.printStackTrace();
            return "持久化失败: " + e.getMessage();
        }
    }


    /**
     * 构建包含上下文的提示词
     * @param conversationId 对话ID
     * @param currentMessage 当前用户消息
     * @return 包含上下文的完整提示词
     */
    private String buildContextPrompt(String conversationId, String currentMessage) {
        StringBuilder contextBuilder = new StringBuilder();

        // 1. 加载概要记录（ChatRecordZip）- 从数据库
        ChatRecordZip recordZip = chatContextService.getChatRecordZip(conversationId);
        if (recordZip != null && recordZip.getCompressedData() != null && !recordZip.getCompressedData().isEmpty()) {
            contextBuilder.append("[对话概要]\n");
            contextBuilder.append(recordZip.getCompressedData());
            contextBuilder.append("\n\n");
        }

        // 2. 优先从缓存加载历史对话记录
        List<org.dee.dto.ChatMessageDTO> cachedMessages = cacheChatService.getCachedChatMessages(conversationId, org.dee.dto.ChatMessageDTO.class);
        
        if (cachedMessages != null && !cachedMessages.isEmpty()) {
            // 从缓存加载
            contextBuilder.append("[最近对话]\n");
            for (org.dee.dto.ChatMessageDTO msg : cachedMessages) {
                contextBuilder.append("用户: ").append(msg.getUserMessage()).append("\n");
                contextBuilder.append("助手: ").append(msg.getBotResponse()).append("\n");
            }
            contextBuilder.append("\n");
        } else {
            // 缓存为空，从数据库加载
            List<ChatRecord> chatRecords = chatContextService.getChatRecords(conversationId);
            if (chatRecords != null && !chatRecords.isEmpty()) {
                contextBuilder.append("[历史对话]\n");
                for (ChatRecord record : chatRecords) {
                    contextBuilder.append("用户: ").append(record.getUserMessage()).append("\n");
                    contextBuilder.append("助手: ").append(record.getBotResponse()).append("\n");
                }
                contextBuilder.append("\n");
            }
        }

        // 3. 添加当前消息
        contextBuilder.append("[当前问题]\n");
        contextBuilder.append(currentMessage);

        return contextBuilder.toString();
    }

}