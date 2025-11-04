package org.dee.service.impl;

import org.dee.annotions.MyTool;
import org.dee.config.DynamicToolCallbackFilter;
import org.dee.entity.SQLTool;
import org.dee.mapper.ToolMapper;
import org.dee.service.ToolService;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ToolServiceImpl implements ToolService {

    @Autowired
    private ToolMapper toolMapper;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private DynamicToolCallbackFilter toolCallbackFilter;

    @Override
    public ToolCallback[] selectToolsForChat() {
        // 使用过滤器获取启用的工具
        return toolCallbackFilter.getEnabledToolCallbacks();
    }

    @Override
    public int loadExistingToolsToDatabase() {
        // 获取所有带有 @MyTool 注解的 Bean
        Map<String, Object> toolBeans = applicationContext.getBeansWithAnnotation(MyTool.class);
        
        List<SQLTool> toolList = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        
        for (Map.Entry<String, Object> entry : toolBeans.entrySet()) {
            Object toolBean = entry.getValue();
            Class<?> toolClass = toolBean.getClass();
            
            // 获取 @MyTool 注解的值作为工具描述
            MyTool myToolAnnotation = toolClass.getAnnotation(MyTool.class);
            
            // 遍历类中的所有方法，查找带有 @Tool 注解的方法
            for (Method method : toolClass.getDeclaredMethods()) {
                if (method.isAnnotationPresent(Tool.class)) {
                    Tool toolAnnotation = method.getAnnotation(Tool.class);

                    SQLTool tool = new SQLTool();
                    tool.setToolName(method.getName());
                    tool.setDescription(toolAnnotation.description());
                    tool.setClassName(toolClass.getName());
                    tool.setMethodName(method.getName());
                    
                    // 获取方法参数信息
                    StringBuilder params = new StringBuilder();
                    Class<?>[] paramTypes = method.getParameterTypes();
                    for (int i = 0; i < paramTypes.length; i++) {
                        if (i > 0) params.append(", ");
                        params.append(paramTypes[i].getSimpleName());
                    }
                    tool.setParameters(params.toString());
                    
                    tool.setEnabled(1); // 默认启用
                    tool.setCategory(extractCategory(toolClass.getName()));
                    tool.setCreatedAt(now);
                    tool.setUpdatedAt(now);
                    
                    toolList.add(tool);
                }
            }
        }
        
        if (!toolList.isEmpty()) {
            // 批量插入到数据库
            return toolMapper.batchInsert(toolList);
        }
        
        return 0;
    }

    @Override
    public List<SQLTool> loadToolsFromDatabase() {
        // 查询所有工具
        return toolMapper.selectList(null);
    }

    @Override
    public List<SQLTool> loadEnabledToolsFromDatabase() {
        // 查询所有启用的工具
        return toolMapper.selectEnabledTools();
    }

    
    @Override
    public boolean toggleToolStatus(Integer id) {
        SQLTool tool = toolMapper.selectById(id);
        if (tool == null) {
            return false;
        }
        // 切换状态：1 -> 0 或 0 -> 1
        tool.setEnabled(tool.getEnabled() == 1 ? 0 : 1);
        tool.setUpdatedAt(LocalDateTime.now());
        return toolMapper.updateById(tool) > 0;
    }

    @Override
    public boolean deleteTool(Integer id) {
        return toolMapper.deleteById(id) > 0;
    }

    @Override
    public ToolCallback[] convertToToolCallbacks(List<SQLTool> sqlTools) {
        if (sqlTools == null || sqlTools.isEmpty()) {
            return new ToolCallback[0];
        }

        List<ToolCallback> callbacks = new ArrayList<>();

        for (SQLTool sqlTool : sqlTools) {
            try {
                // 通过类名获取工具类的 Bean 实例
                Class<?> toolClass = Class.forName(sqlTool.getClassName());
                Object toolBean = applicationContext.getBean(toolClass);

                // 获取方法
                Method[] methods = toolClass.getDeclaredMethods();
                for (Method method : methods) {
                    if (method.getName().equals(sqlTool.getMethodName()) 
                        && method.isAnnotationPresent(Tool.class)) {

                        // 使用 Spring AI 的 ToolCallback 包装方法
                        // 注意：这里需要研究 Spring AI 的 ToolCallback 构造方式
                        // 目前 Spring AI 主要通过 @Tool 注解自动扫描，不直接支持动态创建 ToolCallback
                        
                        // 研究方向 1: 使用 FunctionCallback
                        // ToolCallback callback = FunctionCallback.builder()
                        //     .function(sqlTool.getMethodName(), (input) -> {
                        //         return method.invoke(toolBean, input);
                        //     })
                        //     .description(sqlTool.getDescription())
                        //     .build();
                        
                        // 研究方向 2: 使用 MethodInvokingFunctionCallback (如果存在)
                        // ToolCallback callback = new MethodInvokingFunctionCallback(
                        //     toolBean, method, sqlTool.getDescription()
                        // );
                        
                        // 研究方向 3: 直接使用已注册的 Bean
                        // Spring AI 会自动扫描 @Tool 注解的方法并注册为 ToolCallback
                        // 可以通过 ApplicationContext 获取已注册的 ToolCallback
                        
                        System.out.println("找到工具方法: " + sqlTool.getClassName() + "." + sqlTool.getMethodName());
                    }
                }
            } catch (ClassNotFoundException e) {
                System.err.println("工具类不存在: " + sqlTool.getClassName());
            } catch (Exception e) {
                System.err.println("加载工具失败: " + e.getMessage());
            }
        }

        return callbacks.toArray(new ToolCallback[0]);
    }

    @Override
    public ToolCallback[] getEnabledToolCallbacks() {
        // 使用 DynamicToolCallbackFilter 获取启用的工具
        return toolCallbackFilter.getEnabledToolCallbacks();
        
        /* 
         * ==================== 实现说明 ====================
         * 
         * 已实现方案：使用过滤器模式（DynamicToolCallbackFilter）
         * 
         * 工作流程：
         * 1. Spring AI 自动扫描所有 @Tool 注解的方法并创建 ToolCallback
         * 2. DynamicToolCallbackFilter 从 Spring 容器获取所有 ToolCallback
         * 3. 从数据库查询启用的工具列表
         * 4. 根据方法名匹配，过滤出启用的 ToolCallback
         * 5. 返回过滤后的 ToolCallback 数组供 ChatClient 使用
         * 
         * 使用方式：
         * - 在 ChatController 中调用 toolService.getEnabledToolCallbacks()
         * - 将返回的 ToolCallback[] 传递给 ChatClient.tools() 或 toolCallbacks()
         * 
         * 其他研究方向（如需深入）：
         * 
         * 1. 使用 FunctionCallback 动态创建：
         *    - 需要研究 Spring AI 的 FunctionCallback.builder() API
         *    - 手动处理参数解析和 JSON Schema 生成
         *    - 参考：org.springframework.ai.model.function.FunctionCallback
         * 
         * 2. 自定义 ToolCallback 实现：
         *    - 实现 ToolCallback 接口
         *    - 使用反射调用工具方法
         *    - 需要实现 getName()、getDescription()、call() 等方法
         * 
         * 3. 研究 Spring AI 源码：
         *    - 查看 ToolCallbackProvider 接口
         *    - 研究 AnnotatedToolCallbackProvider 的实现
         *    - 寻找动态注册工具的官方 API
         * 
         * ====================================================
         */
    }
    
    /**
     * 从类名中提取分类
     * @param className 完整类名
     * @return 分类名称
     */
    private String extractCategory(String className) {
        String[] parts = className.split("\\.");
        if (parts.length > 0) {
            String simpleName = parts[parts.length - 1];
            // 移除 "Tool" 后缀
            if (simpleName.endsWith("Tool")) {
                return simpleName.substring(0, simpleName.length() - 4);
            }
            return simpleName;
        }
        return "Unknown";
    }
}
