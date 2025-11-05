package org.dee.service;

import org.dee.entity.ToolAuditLog;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface ToolAuditService {

    /**
     * 记录工具调用开始
     * @param conversationId 对话ID
     * @param requestId 请求ID
     * @param toolName 工具名称
     * @param methodName 方法名称
     * @param parameters 参数
     * @param userId 用户ID
     * @param ipAddress IP地址
     * @return 审计日志ID
     */
    Integer logToolStart(String conversationId, String requestId, String toolName, 
                        String methodName, String parameters, String userId, String ipAddress);

    /**
     * 记录工具调用成功
     * @param logId 日志ID
     * @param result 执行结果
     */
    void logToolSuccess(Integer logId, String result);

    /**
     * 记录工具调用失败
     * @param logId 日志ID
     * @param errorMessage 错误信息
     */
    void logToolFailure(Integer logId, String errorMessage);

    /**
     * 根据对话ID查询审计日志
     * @param conversationId 对话ID
     * @return 审计日志列表
     */
    List<ToolAuditLog> getLogsByConversationId(String conversationId);

    /**
     * 根据工具名称查询审计日志
     * @param toolName 工具名称
     * @param limit 限制数量
     * @return 审计日志列表
     */
    List<ToolAuditLog> getLogsByToolName(String toolName, Integer limit);

    /**
     * 查询指定时间范围内的审计日志
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 审计日志列表
     */
    List<ToolAuditLog> getLogsByTimeRange(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 查询失败的工具调用
     * @param limit 限制数量
     * @return 失败的审计日志列表
     */
    List<ToolAuditLog> getFailedLogs(Integer limit);

    /**
     * 统计工具调用次数
     * @param toolName 工具名称
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 调用次数
     */
    Integer countToolUsage(String toolName, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 获取工具使用统计
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 统计数据
     */
    Map<String, Object> getToolUsageStatistics(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 删除指定时间之前的审计日志
     * @param beforeTime 时间点
     * @return 删除的记录数
     */
    Integer deleteLogsBefore(LocalDateTime beforeTime);
}
