package org.dee.service.impl;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.dee.annotions.MyTool;
import org.dee.dto.ToolInputDTO;
import org.dee.entity.SQLTool;
import org.dee.mapper.ToolMapper;
import org.dee.service.ToolService;
import org.dee.utlis.ToolUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.ai.tool.metadata.ToolMetadata;
import org.springframework.ai.tool.method.MethodToolCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

@Slf4j
@Service
public class ToolServiceImpl implements ToolService {

    @Autowired
    private ToolMapper toolMapper;

    @Autowired
    private ApplicationContext applicationContext;

    @Override
    public List<ToolCallback> selectEnabledToolCallbacks() {


        List<SQLTool> sqlTools = loadEnabledToolsFromDatabase();


        return convertToToolCallbacks(sqlTools);
    }


    @Override
    public int loadExistingToolsToDatabase() {
        try {
            // 获取所有带有 @MyTool 注解的 Bean
            Map<String, Object> toolBeans = applicationContext.getBeansWithAnnotation(MyTool.class);

            List<SQLTool> toolList = new ArrayList<>();
            LocalDateTime now = LocalDateTime.now();

            for (Map.Entry<String, Object> entry : toolBeans.entrySet()) {
                Object toolBean = entry.getValue();
                Class<?> toolClass = toolBean.getClass();


                // 遍历类中的所有方法，查找带有 @Tool 注解的方法
                for (Method method : toolClass.getDeclaredMethods()) {
                    if (method.isAnnotationPresent(Tool.class)) {
                        Tool toolAnnotation = method.getAnnotation(Tool.class);

                        SQLTool tool = new SQLTool();
                        tool.setId(0);
                        tool.setToolName(method.getName());
                        tool.setDescription(toolAnnotation.description());
                        tool.setClassName(toolClass.getName());


                        tool.setInputSchema(ToolUtils.buildInputSchema(method));

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
        } catch (Exception e) {
            log.error("加载工具到数据库失败: " + e.getMessage());
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
    private static MethodToolCallback buildMethodToolCallback(SQLTool sqlTool, Method method,Object toolInstance) {

        ToolDefinition definition = ToolDefinition.builder()
                .name(sqlTool.getToolName())
                .description(sqlTool.getDescription())
                .inputSchema(sqlTool.getInputSchema())
                .build();

        System.out.println("definition: " + definition);
        return MethodToolCallback.builder()
                .toolDefinition(definition)
                .toolMethod(method)
                .toolObject(toolInstance)
                .build();
    }



    public static ToolCallback buildToolFromString(String description,String toolName,String inputSchema){
        try {
            ToolDefinition toolDefinition = ToolDefinition.builder()
                    .name(toolName)
                    .description(description)
                    .inputSchema(inputSchema)
                    .build();
            ToolMetadata toolMetadata = ToolMetadata.builder()
                    .build();

            FunctionToolCallback callback = new FunctionToolCallback<>(
                    toolDefinition,
                    toolMetadata,
                    ToolInputDTO.class,
                    getToolRunningFunction(toolName),
                    null
            );

            return callback;
        }catch (Exception e){
            log.error("通过字符串加载工具失败: " + e.getMessage());
            return null;
        }
    }

    /**
     * 将数据库中的 SQLTool 转换为 ChatClient 可用的 ToolCallback
     * @param sqlTools 数据库中的工具列表
     * @return ToolCallback 数组，可直接用于 ChatClient
     */
    private List<ToolCallback> convertToToolCallbacks(List<SQLTool> sqlTools) {
        if (sqlTools == null || sqlTools.isEmpty()) {
            return new ArrayList<>();
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
                    if (method.getName().equals(sqlTool.getToolName())
                            && method.isAnnotationPresent(Tool.class)) {
                        //  创建 MethodToolCallback 实例
                        MethodToolCallback callback = buildMethodToolCallback(sqlTool, method,toolBean);
                        callbacks.add(callback);
                        System.out.println("找到工具方法: " + sqlTool.getClassName() + "." + sqlTool.getToolName());
                    }
                }
            } catch (ClassNotFoundException e) {
                log.warn("当前工具不是现有工具，尝试通过字符串加载: " + sqlTool.getClassName());

                ToolCallback toolCallback = buildToolFromString(
                        sqlTool.getDescription(),
                        sqlTool.getToolName(),
                        sqlTool.getInputSchema());

                if (toolCallback != null){
                    callbacks.add(toolCallback);
                }else log.error("加载工具失败: " + e.getMessage());

            } catch (Exception e) {
                System.err.println("加载工具失败: " + e.getMessage());
            }
        }

        return callbacks;
    }

    @NotNull
    private static BiFunction<Object, ToolContext, String> getToolRunningFunction(String toolName) {
        return (input, output) -> {
            // 工具逻辑实现
            System.out.println("调用工具 " + toolName + "，输入参数: " + input);
            System.out.println("output 参数的值：" + output);
            System.out.println("output 序列化后:" + JSON.toJSONString(output));
            return "今日 " + input + " 天气晴朗，气温25度";
        };
    }
}
