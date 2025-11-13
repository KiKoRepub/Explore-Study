package org.mcp.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 健康检查控制器
 * 用于验证MCP服务器是否正常运行
 */
@RestController
public class HealthController {
    
    /**
     * 基本健康检查
     */
    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "MCP Server");
        response.put("port", 8085);
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }
    
    /**
     * MCP服务状态检查
     */
    @GetMapping("/mcp/status")
    public Map<String, Object> mcpStatus() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "READY");
        response.put("endpoint", "/mcp/book");
        response.put("type", "SSE");
        response.put("tools", new String[]{"getBookInfo", "getBookStoreInfo", "getBookRentInfo"});
        return response;
    }
}
