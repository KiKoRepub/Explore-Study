package org.mcp.controller;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.mcp.tool.BookTools;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * MCP SSE 控制器
 * 注意：这个控制器主要用于测试，实际的MCP协议由 spring-ai-starter-mcp-server-webflux 自动处理
 * MCP协议的SSE端点会自动注册到配置的 sse-message-endpoint 路径
 */
@Slf4j
@RestController
@RequestMapping("/mcp")
public class SSEController {

    @Autowired
    ToolCallbackProvider bookTools;

    ObjectMapper objectMapper = new ObjectMapper();


    /**
     * 测试用的SSE端点
     * 实际的MCP协议端点由Spring AI MCP自动创建
     */
//    @GetMapping(value = "/book", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> testSse() {
        log.info("测试SSE连接建立");

        return Flux.interval(Duration.ofSeconds(5))
                .map(seq -> {
                    log.debug("注册工具 #{}", seq);
                    return ServerSentEvent.<String>builder()
                            .id(String.valueOf(seq))
                            .event("message")
                            .data(buildToolDefinitionMessage())
                            .build();
                })
                .doOnSubscribe(sub -> log.info("客户端订阅SSE流"))
                .doOnCancel(() -> log.info("客户端取消SSE连接"))
                .doOnError(error -> log.error("SSE连接错误", error));
    }

    private String buildToolDefinitionMessage() {

        ToolCallback[] toolCallbacks = bookTools.getToolCallbacks();

        List<Map<String,String>> result = new ArrayList<>();

        for (ToolCallback toolCallback : toolCallbacks) {
            var toolDef = toolCallback.getToolDefinition();
            result.add(Map.of(
                    "name", toolDef.name(),
                    "description", toolDef.description(),
                    "inputSchema",toolDef.inputSchema()
            ));
        }
        return JSON.toJSONString(result);
    }

}
