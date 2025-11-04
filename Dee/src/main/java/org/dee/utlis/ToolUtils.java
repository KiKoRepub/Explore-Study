package org.dee.utlis;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.tool.annotation.ToolParam;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

public class ToolUtils {

    private static final ObjectMapper objectMapper = new ObjectMapper();


    public static String buildInputSchema(Method method) {
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");

        Map<String, Object> properties = new LinkedHashMap<>();
        List<String> requiredList = new ArrayList<>();

        for (Parameter parameter : method.getParameters()) {
            Map<String, Object> fieldSchema = new LinkedHashMap<>();

            // 处理参数类型
            fieldSchema.put("type", mapJavaTypeToJsonType(parameter.getType()));

            // 处理 @ToolParam 注解（如果存在）
            ToolParam toolParam = parameter.getAnnotation(ToolParam.class);
            if (toolParam != null) {
                if (!toolParam.description().isEmpty()) {
                    fieldSchema.put("description", toolParam.description());
                }
                if (toolParam.required()) {
                    requiredList.add(parameter.getName());
                }
            } else {
                // 没有注解时，也给个默认描述
                fieldSchema.put("description", parameter.getName());
            }

            properties.put(parameter.getName(), fieldSchema);
        }

        schema.put("properties", properties);
        if (!requiredList.isEmpty()) {
            schema.put("required", requiredList);
        }

        try {
            // 格式化输出成 JSON 字符串
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(schema);
        } catch (Exception e) {
            throw new RuntimeException("生成 inputSchema 失败：" + e.getMessage(), e);
        }
    }

    /**
     * Java 类型映射到 JSON Schema 类型
     */
    private static String mapJavaTypeToJsonType(Class<?> type) {
        if (type == String.class) return "string";
        if (Number.class.isAssignableFrom(type)
                || type == int.class || type == long.class
                || type == double.class || type == float.class)
            return "number";
        if (type == boolean.class || type == Boolean.class) return "boolean";
        if (Collection.class.isAssignableFrom(type) || type.isArray()) return "array";
        return "object"; // 默认兜底
    }
}

