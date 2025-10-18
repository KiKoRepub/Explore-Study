package org.deepseek.handler;


import com.alibaba.fastjson.JSONObject;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * JSONObject 类型处理器
 *  用来序列化成数据库中的 VARCHAR 类型
 */
@MappedTypes(JSONObject.class)
@MappedJdbcTypes(JdbcType.VARCHAR)
public class JsonObjectTypeHandler extends BaseTypeHandler<JSONObject> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i,
                                    JSONObject parameter, JdbcType jdbcType)
            throws SQLException {
        // 将 JSONObject 序列化为 JSON 字符串
        String jsonString = parameter.toJSONString();
        ps.setString(i, jsonString);
    }

    @Override
    public JSONObject getNullableResult(ResultSet rs, String columnName)
            throws SQLException {
        String jsonString = rs.getString(columnName);
        return parseJsonObject(jsonString);
    }

    @Override
    public JSONObject getNullableResult(ResultSet rs, int columnIndex)
            throws SQLException {
        String jsonString = rs.getString(columnIndex);
        return parseJsonObject(jsonString);
    }

    @Override
    public JSONObject getNullableResult(CallableStatement cs, int columnIndex)
            throws SQLException {
        String jsonString = cs.getString(columnIndex);
        return parseJsonObject(jsonString);
    }

    private JSONObject parseJsonObject(String jsonString) {
        if (jsonString == null || jsonString.isEmpty()) {
            return new JSONObject();
        }
        return JSONObject.parseObject(jsonString);
    }
}
