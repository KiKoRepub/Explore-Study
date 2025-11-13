package org.dee.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.dee.entity.SQLMcpServer;

/**
 * MCP服务器数据访问层
 */
@Mapper
public interface MCPServerMapper extends BaseMapper<SQLMcpServer> {
}
