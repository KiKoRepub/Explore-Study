package org.dee.controller;

import io.modelcontextprotocol.client.McpAsyncClient;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.dee.callBack.MyMcpToolCallBackProvider;
import org.dee.enums.ErrorCodeEnum;
import org.dee.entity.vo.ResultBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * MCP工具管理控制器
 * 用于查看和测试MCP工具
 */
@Slf4j
@RestController
@RequestMapping("/mcp/tools")
@Api(tags = "MCP管理")
public class MCPToolController {

    @Autowired(required = false)
    private List<McpAsyncClient> mcpAsyncClients;

    @Autowired(required = false)
    private Map<String, MyMcpToolCallBackProvider> mcpToolCallbackProviderMap;

    /**
     * 获取所有MCP客户端信息
     */
    @GetMapping("/clients")
    @ApiOperation(value = "获取MCP客户端列表", notes = "获取所有已连接的MCP客户端信息")
    public ResultBean getClients() {
        if (mcpAsyncClients == null || mcpAsyncClients.isEmpty()) {
            return ResultBean.error(ErrorCodeEnum.FAIL,"未找到任何MCP客户端");
        }

        List<Map<String, Object>> clientInfoList = new ArrayList<>();
        
        for (McpAsyncClient client : mcpAsyncClients) {
            try {
                Map<String, Object> clientInfo = new HashMap<>();
                var serverInfo = client.getServerInfo();
                
                clientInfo.put("name", serverInfo.name());
                clientInfo.put("version", serverInfo.version());
                clientInfo.put("protocolVersion", serverInfo.version());
                
                // 获取工具列表
                var toolsResponse = client.listTools().block();
                if (toolsResponse != null && toolsResponse.tools() != null) {
                    List<Map<String, String>> tools = toolsResponse.tools().stream()
                            .map(tool -> {
                                Map<String, String> toolInfo = new HashMap<>();
                                toolInfo.put("name", tool.name());
                                toolInfo.put("description", tool.description());
                                return toolInfo;
                            })
                            .collect(Collectors.toList());
                    clientInfo.put("tools", tools);
                    clientInfo.put("toolCount", tools.size());
                } else {
                    clientInfo.put("tools", Collections.emptyList());
                    clientInfo.put("toolCount", 0);
                }
                
                clientInfoList.add(clientInfo);
                
            } catch (Exception e) {
                log.error("获取客户端信息失败", e);
                Map<String, Object> errorInfo = new HashMap<>();
                errorInfo.put("error", e.getMessage());
                clientInfoList.add(errorInfo);
            }
        }
        
        return ResultBean.success(clientInfoList);
    }

    /**
     * 获取指定客户端的工具列表
     */
    @GetMapping("/list/{clientName}")
    @ApiOperation(value = "获取指定客户端的工具", notes = "根据客户端名称获取其提供的工具列表")
    public ResultBean getToolsByClient(@PathVariable String clientName) {
        if (mcpAsyncClients == null || mcpAsyncClients.isEmpty()) {
            return ResultBean.error(ErrorCodeEnum.FAIL,"未找到任何MCP客户端");
        }

        Optional<McpAsyncClient> clientOpt = mcpAsyncClients.stream()
                .filter(c -> c.getServerInfo().name().equals(clientName))
                .findFirst();

        if (!clientOpt.isPresent()) {
            return ResultBean.error(ErrorCodeEnum.FAIL,"未找到名为 " + clientName + " 的客户端");
        }

        try {
            McpAsyncClient client = clientOpt.get();
            var toolsResponse = client.listTools().block();
            
            if (toolsResponse == null || toolsResponse.tools() == null) {
                return ResultBean.success(Collections.emptyList());
            }

            List<Map<String, Object>> tools = toolsResponse.tools().stream()
                    .map(tool -> {
                        Map<String, Object> toolInfo = new HashMap<>();
                        toolInfo.put("name", tool.name());
                        toolInfo.put("description", tool.description());
                        toolInfo.put("inputSchema", tool.inputSchema());
                        return toolInfo;
                    })
                    .collect(Collectors.toList());

            return ResultBean.success(tools);
            
        } catch (Exception e) {
            log.error("获取工具列表失败", e);
            return ResultBean.error(ErrorCodeEnum.FAIL,"获取工具列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取所有工具回调提供者信息
     */
    @GetMapping("/providers")
    @ApiOperation(value = "获取工具回调提供者", notes = "获取所有已注册的工具回调提供者信息")
    public ResultBean getProviders() {
        if (mcpToolCallbackProviderMap == null || mcpToolCallbackProviderMap.isEmpty()) {
            return ResultBean.error(ErrorCodeEnum.FAIL,"未找到任何工具回调提供者");
        }

        Map<String, Object> providerInfo = new HashMap<>();
        
        mcpToolCallbackProviderMap.forEach((name, provider) -> {
            Map<String, Object> info = new HashMap<>();
            info.put("toolCount", provider.getCallBackNums());
            
            List<String> toolNames = Arrays.stream(provider.getToolCallbacks())
                    .map(callback -> callback.getToolDefinition().name())
                    .collect(Collectors.toList());
            info.put("tools", toolNames);
            
            providerInfo.put(name, info);
        });

        return ResultBean.success(providerInfo);
    }

    /**
     * 测试MCP连接
     */
    @GetMapping("/test-connection")
    @ApiOperation(value = "测试MCP连接", notes = "测试所有MCP客户端的连接状态")
    public ResultBean testConnection() {
        if (mcpAsyncClients == null || mcpAsyncClients.isEmpty()) {
            return ResultBean.error(ErrorCodeEnum.FAIL,"未找到任何MCP客户端");
        }

        List<Map<String, Object>> results = new ArrayList<>();
        
        for (McpAsyncClient client : mcpAsyncClients) {
            Map<String, Object> result = new HashMap<>();
            
            try {
                var serverInfo = client.getServerInfo();
                result.put("name", serverInfo.name());
                result.put("status", "connected");
                
                // 尝试列出工具
                var toolsResponse = client.listTools().block();
                if (toolsResponse != null && toolsResponse.tools() != null) {
                    result.put("toolCount", toolsResponse.tools().size());
                    result.put("message", "连接正常，工具加载成功");
                } else {
                    result.put("toolCount", 0);
                    result.put("message", "连接正常，但未找到工具");
                }
                
            } catch (Exception e) {
                result.put("status", "error");
                result.put("message", e.getMessage());
                log.error("测试连接失败", e);
            }
            
            results.add(result);
        }
        
        return ResultBean.success(results);
    }

    /**
     * 刷新MCP工具列表
     */
    @PostMapping("/refresh")
    @ApiOperation(value = "刷新工具列表", notes = "重新从MCP服务器获取工具列表")
    public ResultBean refreshTools() {
        if (mcpAsyncClients == null || mcpAsyncClients.isEmpty()) {
            return ResultBean.error(ErrorCodeEnum.FAIL,"未找到任何MCP客户端");
        }

        Map<String, Object> refreshResult = new HashMap<>();
        
        for (McpAsyncClient client : mcpAsyncClients) {
            try {
                var serverInfo = client.getServerInfo();
                var toolsResponse = client.listTools().block();
                
                if (toolsResponse != null && toolsResponse.tools() != null) {
                    refreshResult.put(serverInfo.name(), 
                        "成功刷新，共 " + toolsResponse.tools().size() + " 个工具");
                } else {
                    refreshResult.put(serverInfo.name(), "刷新成功，但未找到工具");
                }
                
            } catch (Exception e) {
                refreshResult.put("error", e.getMessage());
                log.error("刷新工具失败", e);
            }
        }
        
        return ResultBean.success(refreshResult);
    }
}
