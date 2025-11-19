package org.dee.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.dee.entity.dto.McpServerDto;
import org.dee.entity.SQLMcpServer;
import org.dee.enums.ErrorCodeEnum;
import org.dee.service.MCPService;
import org.dee.entity.vo.McpServerVo;
import org.dee.entity.vo.ResultBean;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * MCP服务器管理控制器
 * TODO 完善MCP的管理功能 (Nacos MCP ROUTER )
 */
@Slf4j
@RestController
@RequestMapping("/mcp")
@Api(tags = "MCP数据库管理")
public class MCPSQLController {

    @Autowired
    private MCPService mcpService;

    /**
     * 获取所有MCP服务器列表
     */
    @GetMapping("/server-list")
    @ApiOperation(value = "获取MCP服务器列表", notes = "获取所有已配置的MCP服务器信息")
    public ResultBean getMCPServerList() {
        try {
            List<McpServerVo> serverList = mcpService.getMcpServerList();
            return ResultBean.success(serverList);
        } catch (Exception e) {
            log.error("获取MCP服务器列表失败", e);
            return ResultBean.error(ErrorCodeEnum.SERVICE_ERROR,"获取服务器列表失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID获取MCP服务器详情
     */
    @GetMapping("/server/{id}")
    @ApiOperation(value = "获取MCP服务器详情", notes = "根据ID获取指定MCP服务器的详细信息")
    public ResultBean getMCPServerById(@PathVariable Integer id) {
        try {
            McpServerVo server = mcpService.getMcpServerById(id);
            if (server == null) {
                return ResultBean.error(ErrorCodeEnum.SERVICE_ERROR,"服务器不存在");
            }
            return ResultBean.success(server);
        } catch (Exception e) {
            log.error("获取MCP服务器详情失败", e);
            return ResultBean.error(ErrorCodeEnum.SERVICE_ERROR,"获取服务器详情失败: " + e.getMessage());
        }
    }

    /**
     * 添加MCP服务器
     */
    @PostMapping("/add-server")
    @ApiOperation(value = "添加MCP服务器", notes = "添加新的MCP服务器配置")
    public ResultBean addMCPServer(@Valid @RequestBody McpServerDto mcpServerDto) {
        try {
            SQLMcpServer mcpServer = new SQLMcpServer();
            BeanUtils.copyProperties(mcpServerDto, mcpServer);
            
            boolean success = mcpService.addMcpServer(mcpServer);
            if (success) {
                return ResultBean.success("添加成功");
            }
            return ResultBean.error(ErrorCodeEnum.SERVICE_ERROR,"添加失败");
        } catch (Exception e) {
            log.error("添加MCP服务器失败", e);
            return ResultBean.error(ErrorCodeEnum.SERVICE_ERROR,"添加服务器失败: " + e.getMessage());
        }
    }

    /**
     * 更新MCP服务器
     */
    @PutMapping("/update-server")
    @ApiOperation(value = "更新MCP服务器", notes = "更新已存在的MCP服务器配置")
    public ResultBean updateMCPServer(@Valid @RequestBody McpServerDto mcpServerDto) {
        try {
            if (mcpServerDto.getId() == null) {
                return ResultBean.error(ErrorCodeEnum.SERVICE_ERROR,"服务器ID不能为空");
            }
            
            SQLMcpServer mcpServer = new SQLMcpServer();
            BeanUtils.copyProperties(mcpServerDto, mcpServer);
            
            boolean success = mcpService.updateMcpServer(mcpServer);
            if (success) {
                return ResultBean.success("更新成功");
            }
            return ResultBean.error(ErrorCodeEnum.SERVICE_ERROR,"更新失败");
        } catch (Exception e) {
            log.error("更新MCP服务器失败", e);
            return ResultBean.error(ErrorCodeEnum.SERVICE_ERROR,"更新服务器失败: " + e.getMessage());
        }
    }

    /**
     * 删除MCP服务器
     */
    @DeleteMapping("/server/{id}")
    @ApiOperation(value = "删除MCP服务器", notes = "根据ID删除指定的MCP服务器")
    public ResultBean deleteMCPServer(@PathVariable Integer id) {
        try {
            boolean success = mcpService.deleteMcpServer(id);
            if (success) {
                return ResultBean.success("删除成功");
            }
            return ResultBean.error(ErrorCodeEnum.SERVICE_ERROR,"删除失败");
        } catch (Exception e) {
            log.error("删除MCP服务器失败", e);
            return ResultBean.error(ErrorCodeEnum.SERVICE_ERROR,"删除服务器失败: " + e.getMessage());
        }
    }

    /**
     * 启用/禁用MCP服务器
     */
    @PatchMapping("/server/{id}/toggle")
    @ApiOperation(value = "启用/禁用MCP服务器", notes = "切换MCP服务器的启用状态")
    public ResultBean toggleMCPServer(@PathVariable Integer id, 
                                      @RequestParam Boolean enabled) {
        try {
            boolean success = mcpService.toggleMcpServer(id, enabled);
            if (success) {
                return ResultBean.success(enabled ? "已启用" : "已禁用");
            }
            return ResultBean.error(ErrorCodeEnum.SERVICE_ERROR,"操作失败");
        } catch (Exception e) {
            log.error("切换MCP服务器状态失败", e);
            return ResultBean.error(ErrorCodeEnum.SERVICE_ERROR,"操作失败: " + e.getMessage());
        }
    }

    /**
     * 测试MCP服务器连接
     */
    @GetMapping("/server/{id}/test")
    @ApiOperation(value = "测试MCP服务器连接", notes = "测试指定MCP服务器的连接状态")
    public ResultBean testConnection(@PathVariable Integer id) {
        try {
            boolean result = mcpService.testConnectionById(id);

            return result ? ResultBean.success("连接成功")
                          : ResultBean.error(ErrorCodeEnum.SERVICE_ERROR,"连接失败");
        } catch (Exception e) {
            log.error("测试MCP服务器连接失败", e);
            return ResultBean.error(ErrorCodeEnum.SERVICE_ERROR,"测试连接失败: " + e.getMessage());
        }
    }

}
