package org.dee.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.modelcontextprotocol.client.McpAsyncClient;
import lombok.extern.slf4j.Slf4j;
import org.dee.callBack.MyMcpToolCallBackProvider;
import org.dee.entity.SQLMcpServer;
import org.dee.mapper.MCPServerMapper;
import org.dee.service.MCPService;
import org.dee.vo.McpServerVo;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class MCPServiceImpl implements MCPService {

    @Autowired
    private MCPServerMapper mcpServerMapper;
    
    @Autowired(required = false)
    private List<McpAsyncClient> mcpAsyncClients;
    @Autowired
    private Map<String, MyMcpToolCallBackProvider> mcpToolCallbackProviderMap;

    @Override
    public List<McpServerVo> getMcpServerList() {
        List<SQLMcpServer> serverList = mcpServerMapper.selectList(null);
        
        return serverList.stream().map(server -> {
            McpServerVo vo = new McpServerVo();
            BeanUtils.copyProperties(server, vo);
            
            // 设置运行时状态
            vo.setConnectionStatus(getConnectionStatus(server));
            vo.setToolCount(getToolCount(server.getServerName()));
            
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public List<ToolCallback> getUserSelectedToolCallbacks(List<String> mcpNames) {
        List<ToolCallback> result = new ArrayList<>();
        for (String mcpName : mcpNames) {
            ToolCallbackProvider provider = mcpToolCallbackProviderMap.get(mcpName);
            result.addAll(List.of(provider.getToolCallbacks()));
        }

        return result;
    }

    @Override
    public McpServerVo getMcpServerById(Integer id) {
        SQLMcpServer server = mcpServerMapper.selectById(id);
        if (server == null) {
            return null;
        }
        
        McpServerVo vo = new McpServerVo();
        BeanUtils.copyProperties(server, vo);
        vo.setConnectionStatus(getConnectionStatus(server));
        vo.setToolCount(getToolCount(server.getServerName()));
        
        return vo;
    }

    @Override
    public boolean addMcpServer(SQLMcpServer mcpServer) {
        mcpServer.setCreateTime(LocalDateTime.now());
        mcpServer.setUpdateTime(LocalDateTime.now());
        if (mcpServer.getEnabled() == null) {
            mcpServer.setEnabled(true);
        }


        return mcpServerMapper.insert(mcpServer) > 0;
    }

    @Override
    public boolean updateMcpServer(SQLMcpServer mcpServer) {
        mcpServer.setUpdateTime(LocalDateTime.now());
        return mcpServerMapper.updateById(mcpServer) > 0;
    }

    @Override
    public boolean deleteMcpServer(Integer id) {
        return mcpServerMapper.deleteById(id) > 0;
    }

    @Override
    public boolean toggleMcpServer(Integer id, Boolean enabled) {
        SQLMcpServer server = new SQLMcpServer();
        server.setId(id);
        server.setEnabled(enabled);
        server.setUpdateTime(LocalDateTime.now());
        return mcpServerMapper.updateById(server) > 0;
    }

    @Override
    public String testConnection(Integer id) {
        SQLMcpServer server = mcpServerMapper.selectById(id);
        if (server == null) {
            return "服务器不存在";
        }
        
        try {
            // TODO: 实现实际的连接测试逻辑
            // 根据不同的服务器类型进行连接测试
            switch (server.getType()) {
                case SSE:
                    return testSseConnection(server);
                case HTTP:
                    return testHttpConnection(server);
                case STDIO:
                    return testStdioConnection(server);
                default:
                    return "未知的服务器类型";
            }
        } catch (Exception e) {
            log.error("测试MCP服务器连接失败: {}", e.getMessage(), e);
            return "连接失败: " + e.getMessage();
        }
    }
    
    /**
     * 获取服务器连接状态
     */
    private String getConnectionStatus(SQLMcpServer server) {
        if (!server.getEnabled()) {
            return "已禁用";
        }
        
        // TODO: 实现实际的状态检查逻辑
        // 可以通过检查 mcpAsyncClients 中是否有对应的客户端来判断
        if (mcpAsyncClients != null && !mcpAsyncClients.isEmpty()) {
            return "已连接";
        }
        
        return "未连接";
    }
    
    /**
     * 获取服务器可用工具数量
     */
    private Integer getToolCount(String serverName) {
        // TODO: 实现实际的工具数量统计逻辑
        // 可以通过查询对应的 McpAsyncClient 获取工具列表
        MyMcpToolCallBackProvider provider = mcpToolCallbackProviderMap.get(serverName);
        if (provider == null){
            log.warn("未找到MCP工具: {}", serverName);
            return 0;
        }
        return provider.getCallBackNums();
    }
    
    /**
     * 测试SSE连接
     */
    private String testSseConnection(SQLMcpServer server) {
        // TODO: 实现SSE连接测试
        return "SSE连接测试功能待实现";
    }
    
    /**
     * 测试HTTP连接
     */
    private String testHttpConnection(SQLMcpServer server) {
        // TODO: 实现HTTP连接测试
        return "HTTP连接测试功能待实现";
    }
    
    /**
     * 测试STDIO连接
     */
    private String testStdioConnection(SQLMcpServer server) {
        // TODO: 实现STDIO连接测试
        return "STDIO连接测试功能待实现";
    }
}
