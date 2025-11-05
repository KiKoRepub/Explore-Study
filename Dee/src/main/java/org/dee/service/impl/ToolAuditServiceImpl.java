package org.dee.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.dee.entity.ToolAuditLog;
import org.dee.mapper.ToolAuditLogMapper;
import org.dee.service.ToolAuditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ToolAuditServiceImpl implements ToolAuditService {

    @Autowired
    private ToolAuditLogMapper toolAuditLogMapper;

    @Override
    public Integer logToolStart(String conversationId, String requestId, String toolName,
                               String methodName, String parameters, String userId, String ipAddress) {
        ToolAuditLog log = new ToolAuditLog();
        log.setConversationId(conversationId);
        log.setRequestId(requestId);
        log.setToolName(toolName);
        log.setMethodName(methodName);
        log.setStatus("STARTED");
        log.setParameters(parameters);
        log.setUserId(userId);
        log.setIpAddress(ipAddress);
        
        LocalDateTime now = LocalDateTime.now();
        log.setCreatedAt(now);
        log.setStartAt(now);
        
        toolAuditLogMapper.insert(log);
        return log.getId();
    }

    @Override
    public void logToolSuccess(Integer logId, String result) {
        ToolAuditLog log = toolAuditLogMapper.selectById(logId);
        if (log != null) {
            LocalDateTime endTime = LocalDateTime.now();
            log.setStatus("COMPLETED");
            log.setResult(result);
            log.setEndAt(endTime);
            
            // 计算执行时间
            if (log.getStartAt() != null) {
                long executionTime = java.time.Duration.between(log.getStartAt(), endTime).toMillis();
                log.setExecutionTime(executionTime);
            }
            
            toolAuditLogMapper.updateById(log);
        }
    }

    @Override
    public void logToolFailure(Integer logId, String errorMessage) {
        ToolAuditLog log = toolAuditLogMapper.selectById(logId);
        if (log != null) {
            LocalDateTime endTime = LocalDateTime.now();
            log.setStatus("FAILED");
            log.setErrorMessage(errorMessage);
            log.setEndAt(endTime);
            
            // 计算执行时间
            if (log.getStartAt() != null) {
                long executionTime = java.time.Duration.between(log.getStartAt(), endTime).toMillis();
                log.setExecutionTime(executionTime);
            }
            
            toolAuditLogMapper.updateById(log);
        }
    }

    @Override
    public List<ToolAuditLog> getLogsByConversationId(String conversationId) {
        return toolAuditLogMapper.selectByConversationId(conversationId);
    }

    @Override
    public List<ToolAuditLog> getLogsByToolName(String toolName, Integer limit) {
        return toolAuditLogMapper.selectByToolName(toolName, limit);
    }

    @Override
    public List<ToolAuditLog> getLogsByTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        return toolAuditLogMapper.selectByTimeRange(startTime, endTime);
    }

    @Override
    public List<ToolAuditLog> getFailedLogs(Integer limit) {
        return toolAuditLogMapper.selectFailedLogs(limit);
    }

    @Override
    public Integer countToolUsage(String toolName, LocalDateTime startTime, LocalDateTime endTime) {
        return toolAuditLogMapper.countToolUsage(toolName, startTime, endTime);
    }

    @Override
    public Map<String, Object> getToolUsageStatistics(LocalDateTime startTime, LocalDateTime endTime) {
        Map<String, Object> statistics = new HashMap<>();
        
        // 查询时间范围内的所有日志
        List<ToolAuditLog> logs = toolAuditLogMapper.selectByTimeRange(startTime, endTime);
        
        // 总调用次数
        statistics.put("totalCalls", logs.size());
        
        // 成功次数
        long successCount = logs.stream()
                .filter(log -> "COMPLETED".equals(log.getStatus()))
                .count();
        statistics.put("successCount", successCount);
        
        // 失败次数
        long failureCount = logs.stream()
                .filter(log -> "FAILED".equals(log.getStatus()))
                .count();
        statistics.put("failureCount", failureCount);
        
        // 平均执行时间
        double avgExecutionTime = logs.stream()
                .filter(log -> log.getExecutionTime() != null)
                .mapToLong(ToolAuditLog::getExecutionTime)
                .average()
                .orElse(0.0);
        statistics.put("avgExecutionTime", avgExecutionTime);
        
        // 按工具名称统计
        Map<String, Long> toolUsageCount = new HashMap<>();
        for (ToolAuditLog log : logs) {
            String toolName = log.getToolName();
            toolUsageCount.put(toolName, toolUsageCount.getOrDefault(toolName, 0L) + 1);
        }
        statistics.put("toolUsageCount", toolUsageCount);
        
        // 成功率
        double successRate = logs.isEmpty() ? 0.0 : (double) successCount / logs.size() * 100;
        statistics.put("successRate", String.format("%.2f%%", successRate));
        
        return statistics;
    }

    @Override
    public Integer deleteLogsBefore(LocalDateTime beforeTime) {
        LambdaQueryWrapper<ToolAuditLog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.lt(ToolAuditLog::getCreatedAt, beforeTime);
        return toolAuditLogMapper.delete(queryWrapper);
    }
}
