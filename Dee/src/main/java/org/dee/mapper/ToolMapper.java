package org.dee.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.dee.entity.SQLTool;

import java.util.List;

@Mapper
public interface ToolMapper extends BaseMapper<SQLTool> {
    
    /**
     * 批量插入工具
     * @param toolList 工具列表
     * @return 插入的记录数
     */
    int batchInsert(@Param("list") List<SQLTool> toolList);
    
    /**
     * 查询所有启用的工具
     * @return 启用的工具列表
     */
    List<SQLTool> selectEnabledTools();
}
