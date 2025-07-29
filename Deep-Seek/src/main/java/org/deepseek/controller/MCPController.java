package org.deepseek.controller;

import io.modelcontextprotocol.client.McpAsyncClient;
import io.modelcontextprotocol.spec.McpSchema;
import org.deepseek.callback.AMapToolCallbackProvider;
import org.deepseek.entity.BookRentInfo;
import org.deepseek.utils.LoggerUtils;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.mcp.client.autoconfigure.NamedClientMcpTransport;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/mcp")
public class MCPController extends AIController{
//    @Autowired
//    @Qualifier("aMapToolCallbackProvider")
//    ToolCallbackProvider aMapToolProvider;
    @Autowired
    @Qualifier("mcpAsyncToolCallbacks")
    ToolCallbackProvider mcpToolProvider;



    public MCPController(OpenAiChatModel openAiChatModel) {
        super(openAiChatModel);
    }

    @GetMapping("/push")
    public String push(@RequestParam("message") String message) {
        ChatResponse response = chatClient.prompt()
                .user(message)
                .toolCallbacks(mcpToolProvider)
                .call()
                .chatResponse();
//
//
        String text = response.getResult().getOutput().getText();
//        System.out.println("text = " + text);
//        return text;
//        System.out.println(mcpToolProvider);
        System.out.println("mcpToolProvider = " + mcpToolProvider);
        System.out.println(mcpToolProvider.getToolCallbacks().length);

        String infos = chatClient.prompt()
                .user(message)
                .toolCallbacks(mcpToolProvider)
                .call()
                .content();

        System.out.println(infos);
//        for (BookRentInfo info : infos) {
//            System.out.println(info);
//        }
        return infos;
    }

    @Autowired
    private List<McpAsyncClient> mcpAsyncClients;
    @PostMapping("/amap/weather")
    private void getMcpAMapTool(@RequestParam("city") String city){
        McpAsyncClient mcpAsyncClient = mcpAsyncClients.get(0);
        Mono<McpSchema.CallToolResult> resultMono = mcpAsyncClient.listTools()
                .flatMap(tools -> {
            return mcpAsyncClient.callTool(
                    new McpSchema.CallToolRequest(
                            "maps_weather",
                            Map.of("city", city)
                    )
            );
        });

        resultMono.subscribe(result -> {
//            LoggerUtils.info("result = " + result);
            System.out.println("result = " + result);
        });
    }


}
