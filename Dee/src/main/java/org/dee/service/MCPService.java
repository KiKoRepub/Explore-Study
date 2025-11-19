package org.dee.service;

import org.dee.entity.SQLMcpServer;
import org.dee.entity.vo.McpServerVo;
import org.springframework.ai.tool.ToolCallback;

import java.util.List;

public interface MCPService {
    
    /**
     * 获取所有MCP服务器列表
     * @return MCP服务器VO列表
     */
    List<McpServerVo> getMcpServerList();
    List<ToolCallback> getUserSelectedToolCallbacks(List<String> mcpNames);
    /**
     * 根据ID获取MCP服务器
     * @param id 服务器ID
     * @return MCP服务器VO
     */
    McpServerVo getMcpServerById(Integer id);
    
    /**
     * 添加MCP服务器
     * @param mcpServer MCP服务器实体
     * @return 是否成功
     */
    boolean addMcpServer(SQLMcpServer mcpServer);
    
    /**
     * 更新MCP服务器
     * @param mcpServer MCP服务器实体
     * @return 是否成功
     */
    boolean updateMcpServer(SQLMcpServer mcpServer);
    
    /**
     * 删除MCP服务器
     * @param id 服务器ID
     * @return 是否成功
     */
    boolean deleteMcpServer(Integer id);
    
    /**
     * 启用/禁用MCP服务器
     * @param id 服务器ID
     * @param enabled 是否启用
     * @return 是否成功
     */
    boolean toggleMcpServer(Integer id, Boolean enabled);
    
    /**
     * 测试MCP服务器连接
     * @param id 服务器ID
     * @return 连接状态信息
     */
    boolean testConnectionById(Integer id);
    boolean testConnection(SQLMcpServer server);
}
