package org.dee.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.dee.entity.ToolAuditLog;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface ToolAuditLogMapper extends BaseMapper<ToolAuditLog> {

    /**
     * 根据对话ID查询审计日志
     * @param conversationId 对话ID
     * @return 审计日志列表
     */
    List<ToolAuditLog> selectByConversationId(@Param("conversationId") String conversationId);

    /**
     * 根据工具名称查询审计日志
     * @param toolName 工具名称
     * @param limit 限制数量
     * @return 审计日志列表
     */
    List<ToolAuditLog> selectByToolName(@Param("toolName") String toolName, @Param("limit") Integer limit);

    /**
     * 查询指定时间范围内的审计日志
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 审计日志列表
     */
    List<ToolAuditLog> selectByTimeRange(@Param("startTime") LocalDateTime startTime, 
                                         @Param("endTime") LocalDateTime endTime);

    /**
     * 统计工具调用次数
     * @param toolName 工具名称
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 调用次数
     */
    Integer countToolUsage(@Param("toolName") String toolName,
                          @Param("startTime") LocalDateTime startTime,
                          @Param("endTime") LocalDateTime endTime);

    /**
     * 查询失败的工具调用
     * @param limit 限制数量
     * @return 失败的审计日志列表
     */
    List<ToolAuditLog> selectFailedLogs(@Param("limit") Integer limit);

    /**
     * 批量插入审计日志
     * @param logs 审计日志列表
     * @return 插入的记录数
     */
    int batchInsert(@Param("logs") List<ToolAuditLog> logs);
}
