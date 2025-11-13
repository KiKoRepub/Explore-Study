package org.dee.service;

import org.dee.entity.SQLTool;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;

import java.util.List;

public interface ToolService {
    /**
     * 获取启用的工具并转换为 ToolCallback
     * @return ToolCallback 数组，可直接用于 ChatClient
     */
    List<ToolCallback> selectEnabledToolCallbacks();
    /**
     * 将已存在的工具加载到数据库中
     * @return 成功加载的工具数量
     */
    int loadExistingToolsToDatabase();
    
    /**
     * 从数据库加载工具
     * @return 工具列表
     */
    List<SQLTool> loadToolsFromDatabase();
    
    /**
     * 从数据库加载启用的工具
     * @return 启用的工具列表
     */
    List<SQLTool> loadTotalEnabledToolsFromDatabase();
    List<SQLTool> loadEnabledToolsFromDatabase(String userId);
    /**
     * 切换工具的启用状态
     * @param id 工具ID
     * @return 是否成功
     */
    boolean toggleToolStatus(Integer id);
    
    /**
     * 删除工具
     * @param id 工具ID
     * @return 是否成功
     */
    boolean deleteTool(Integer id);



    boolean addMcpTool(ToolCallbackProvider provider,String serverName);

}
