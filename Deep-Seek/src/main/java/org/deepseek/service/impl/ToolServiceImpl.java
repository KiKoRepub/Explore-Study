package org.deepseek.service.impl;

import com.alibaba.fastjson.JSONObject;
import org.deepseek.service.ToolService;
import org.deepseek.tools.GetWeatherTool;
import org.deepseek.utils.LoggerUtils;
import org.springframework.ai.document.Document;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.ai.tool.metadata.ToolMetadata;
import org.springframework.ai.tool.method.MethodToolCallback;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ReflectionUtils;


import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ToolServiceImpl implements ToolService {


    @Autowired
    private VectorStore vectorStore;

    /**
     * 获取工具
     * 应用场景 为 解决 传递过多工具给 AI 导致的 工具选择困难
     *    1. 将所有的 Tools 描述信息 存入 RAG 数据库
     *    2. 通过 RAG 模型 获取工具(与对话相似的工具)
     *    3. 动态设置 配置给 AI 的工具数
     *
     * @return
     */
    public List<ToolCallback> getTools(){
        List<ToolCallback> result = new ArrayList<>();
    try {



        result.add(getTool());

    }catch (Exception e){
        e.printStackTrace();

    }
        return result;
    }



    public boolean addToolToVector(ToolCallback toolCallback){
        ToolDefinition definition = toolCallback.getToolDefinition();
        String documentIdOfToolName = definition.name();
        String documentTextOfDescription = definition.description();

        String schema = definition.inputSchema();
        Map<String,Object> metaData = new HashMap<>();

        if (toolCallback instanceof  MethodToolCallback methodCallback){

        }


        metaData.put("schema", schema);
        metaData.put("source","工具库");

        try {
        Document document = new Document(documentIdOfToolName,
                documentTextOfDescription,
                metaData
        );

        vectorStore.add(List.of(document));
            return true;
        }catch (Exception e){
            LoggerUtils.error(e,"添加失败");
            return false;
        }

    }


    public List<ToolCallback> getTool(String prompt,Method toolMethod){

        List<Document> documentList = vectorStore.similaritySearch(SearchRequest.builder()
                .query(prompt)
                .build());
        List<ToolCallback> result = new ArrayList<>();
        for (Document document : documentList) {

            Map<String, Object> metadata = document.getMetadata();

            String schema = (String) metadata.get("schema");
            String name = document.getId();
            String description = document.getText();

            ToolCallback toolCallback = MethodToolCallback.builder()
                    .toolDefinition(ToolDefinition.builder()
                            .name(name)
                            .description(description)
                            .inputSchema(schema)
                            .build())
                    .toolMethod(toolMethod)
                    .toolObject(new GetWeatherTool()) // 这里注入的对象如果需要引用其他对象，要使用spring容器注入
                    .build();


            result.add(toolCallback);
        }

        return result;
    }
    public ToolCallback getTool(){
        Method toolMethod = ReflectionUtils.findMethod(GetWeatherTool.class, "getWeatherInfo", String.class);
        // 创建工具定义
        /*
        name : 工具名称
        description : 工具描述
        inputSchema : 输入参数
         */
        ToolDefinition definition = ToolDefinition.builder()
                .name("getWeatherInfo")
                .description("天气查询工具")
                .inputSchema("""
                        {
                            "type":"Object",
                            "properties":{
                                "location":{
                                    "type":"string",
                                    "description":"特定的地区"
                                }
                            }
                            "required":["location"]
                        }
                        """)
                .build();



        ToolCallback toolCallback = MethodToolCallback.builder()
                .toolDefinition(definition)
                .toolMethod(toolMethod)
                .toolObject(new GetWeatherTool()) // 这里注入的对象如果需要引用其他对象，要使用spring容器注入
                .build();

        return toolCallback;

    }
}
