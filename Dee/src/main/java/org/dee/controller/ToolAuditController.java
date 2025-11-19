package org.dee.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.dee.entity.ToolAuditLog;
import org.dee.enums.ErrorCodeEnum;
import org.dee.service.ToolAuditService;
import org.dee.entity.vo.ResultBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@Api(tags = "工具审计管理")
@RequestMapping("/tool-audit")
public class ToolAuditController {

    @Autowired
    private ToolAuditService toolAuditService;

    /**
     * 根据对话ID查询审计日志
     */
    @GetMapping("/conversation/{conversationId}")
    @ApiOperation(value = "根据对话ID查询审计日志", notes = "查询指定对话中的所有工具调用记录")
    public ResultBean<List<ToolAuditLog>> getLogsByConversationId(@PathVariable String conversationId) {
        try {
            List<ToolAuditLog> logs = toolAuditService.getLogsByConversationId(conversationId);
            return ResultBean.success(logs);
        } catch (Exception e) {
            return ResultBean.error(ErrorCodeEnum.FAIL, "查询失败: " + e.getMessage());
        }
    }

    /**
     * 根据工具名称查询审计日志
     */
    @GetMapping("/tool/{toolName}")
    @ApiOperation(value = "根据工具名称查询审计日志", notes = "查询指定工具的调用记录")
    public ResultBean<List<ToolAuditLog>> getLogsByToolName(
            @PathVariable String toolName,
            @RequestParam(defaultValue = "100") Integer limit) {
        try {
            List<ToolAuditLog> logs = toolAuditService.getLogsByToolName(toolName, limit);
            return ResultBean.success(logs);
        } catch (Exception e) {
            return ResultBean.error(ErrorCodeEnum.FAIL, "查询失败: " + e.getMessage());
        }
    }

    /**
     * 查询指定时间范围内的审计日志
     */
    @GetMapping("/time-range")
    @ApiOperation(value = "查询时间范围内的审计日志", notes = "查询指定时间段内的所有工具调用记录")
    public ResultBean<List<ToolAuditLog>> getLogsByTimeRange(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        try {
            List<ToolAuditLog> logs = toolAuditService.getLogsByTimeRange(startTime, endTime);
            return ResultBean.success(logs);
        } catch (Exception e) {
            return ResultBean.error(ErrorCodeEnum.FAIL, "查询失败: " + e.getMessage());
        }
    }

    /**
     * 查询失败的工具调用
     */
    @GetMapping("/failed")
    @ApiOperation(value = "查询失败的工具调用", notes = "查询所有执行失败的工具调用记录")
    public ResultBean<List<ToolAuditLog>> getFailedLogs(
            @RequestParam(defaultValue = "50") Integer limit) {
        try {
            List<ToolAuditLog> logs = toolAuditService.getFailedLogs(limit);
            return ResultBean.success(logs);
        } catch (Exception e) {
            return ResultBean.error(ErrorCodeEnum.FAIL, "查询失败: " + e.getMessage());
        }
    }

    /**
     * 统计工具调用次数
     */
    @GetMapping("/count/{toolName}")
    @ApiOperation(value = "统计工具调用次数", notes = "统计指定工具在时间范围内的调用次数")
    public ResultBean<Map<String, Object>> countToolUsage(
            @PathVariable String toolName,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        try {
            Integer count = toolAuditService.countToolUsage(toolName, startTime, endTime);
            Map<String, Object> result = Map.of(
                    "toolName", toolName,
                    "count", count,
                    "startTime", startTime != null ? startTime.toString() : "不限",
                    "endTime", endTime != null ? endTime.toString() : "不限"
            );
            return ResultBean.success(result);
        } catch (Exception e) {
            return ResultBean.error(ErrorCodeEnum.FAIL, "统计失败: " + e.getMessage());
        }
    }

    /**
     * 获取工具使用统计
     */
    @GetMapping("/statistics")
    @ApiOperation(value = "获取工具使用统计", notes = "获取指定时间范围内的工具使用统计信息")
    public ResultBean<Map<String, Object>> getToolUsageStatistics(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        try {
            Map<String, Object> statistics = toolAuditService.getToolUsageStatistics(startTime, endTime);
            return ResultBean.success(statistics);
        } catch (Exception e) {
            return ResultBean.error(ErrorCodeEnum.FAIL, "统计失败: " + e.getMessage());
        }
    }

    /**
     * 获取最近24小时的统计
     */
    @GetMapping("/statistics/recent")
    @ApiOperation(value = "获取最近24小时统计", notes = "获取最近24小时的工具使用统计")
    public ResultBean<Map<String, Object>> getRecentStatistics() {
        try {
            LocalDateTime endTime = LocalDateTime.now();
            LocalDateTime startTime = endTime.minusHours(24);
            Map<String, Object> statistics = toolAuditService.getToolUsageStatistics(startTime, endTime);
            return ResultBean.success(statistics);
        } catch (Exception e) {
            return ResultBean.error(ErrorCodeEnum.FAIL, "统计失败: " + e.getMessage());
        }
    }

    /**
     * 删除指定时间之前的审计日志
     */
    @DeleteMapping("/cleanup")
    @ApiOperation(value = "清理历史审计日志", notes = "删除指定时间之前的审计日志")
    public ResultBean<Map<String, Object>> deleteLogsBefore(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime beforeTime) {
        try {
            Integer deletedCount = toolAuditService.deleteLogsBefore(beforeTime);
            Map<String, Object> result = Map.of(
                    "deletedCount", deletedCount,
                    "beforeTime", beforeTime.toString()
            );
            return ResultBean.success("成功删除 " + deletedCount + " 条记录", result);
        } catch (Exception e) {
            return ResultBean.error(ErrorCodeEnum.FAIL, "删除失败: " + e.getMessage());
        }
    }
}
