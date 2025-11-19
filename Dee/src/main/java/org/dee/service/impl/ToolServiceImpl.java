package org.dee.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.dee.annotions.MyTool;
import org.dee.entity.dto.ToolInputDTO;
import org.dee.entity.SQLTool;
import org.dee.enums.ToolTypeEnum;
import org.dee.mapper.ToolMapper;
import org.dee.service.ToolService;
import org.dee.utlis.ToolUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
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


        List<SQLTool> sqlTools = loadTotalEnabledToolsFromDatabase();


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
                        tool.setCategory(ToolTypeEnum.STANDARD.value);
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
    public List<SQLTool> loadTotalEnabledToolsFromDatabase() {

        return toolMapper.selectEnabledTools();
    }

    @Override
    public List<SQLTool> loadEnabledToolsFromDatabase(String userId) {
        // 查询所有启用的工具
        // 仅包括 非 MCP 工具
        /*
        SELECT * FROM sql_tool
        WHERE enabled = 1
          AND (category = 'STANDARD'
              OR (
                  category = 'USER'
                 AND
                  class_name = #{userId}
              )
          );
         */
        LambdaQueryWrapper<SQLTool> wrapper = new LambdaQueryWrapper<>();

        wrapper.eq(SQLTool::getEnabled, 1)
                .and(wrapper1 -> wrapper1
                        .eq(SQLTool::getCategory, ToolTypeEnum.STANDARD.value)
                        .or(wrapper2 -> wrapper2
                                .eq(SQLTool::getCategory, ToolTypeEnum.USER.value)
                                .eq(SQLTool::getClassName, userId)));

        return toolMapper.selectList(wrapper);
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
    public boolean addMcpTool(ToolCallbackProvider provider,String serverName) {
        // MCP工具添加逻辑
        ToolCallback[] toolCallbacks = provider.getToolCallbacks();

        List<SQLTool> mcpToolList = new ArrayList<>();
        for (ToolCallback toolCallback : toolCallbacks) {
            SQLTool tool = new SQLTool();
            ToolDefinition toolDefinition = toolCallback.getToolDefinition();

            tool.setToolName(toolDefinition.name());
            tool.setDescription(toolDefinition.description());
            tool.setInputSchema(toolDefinition.inputSchema());


            tool.setCreatedAt(LocalDateTime.now());
            tool.setUpdatedAt(LocalDateTime.now());
            tool.setEnabled(1);

            tool.setClassName(serverName);
            tool.setCategory(ToolTypeEnum.MCP.value);

            mcpToolList.add(tool);


        }

        if (!mcpToolList.isEmpty()) {
            int inserted = toolMapper.batchInsert(mcpToolList);
            return inserted > 0;
        }

        return false;
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
