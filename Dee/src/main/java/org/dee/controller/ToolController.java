package org.dee.controller;

import com.alibaba.fastjson.JSON;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.dee.entity.SQLTool;
import org.dee.enums.ErrorCodeEnum;
import org.dee.service.ToolService;
import org.dee.entity.vo.ResultBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/tool")
@Api(tags = "工具管理")
public class ToolController {

    @Autowired
    private ToolService toolService;

    /**
     * 将已存在的工具加载到数据库
     */
    @PostMapping("/toDatabase")
    @ApiOperation(value = "加载工具到数据库", notes = "扫描所有带@MyTool注解的类，将工具信息保存到数据库")
    public ResultBean<Map<String, Object>> loadToolsToDatabase() {
        try {
            int count = toolService.loadExistingToolsToDatabase();
            Map<String, Object> data = new HashMap<>();
            data.put("count", count);
            return ResultBean.success("成功加载 " + count + " 个工具到数据库", data);
        } catch (Exception e) {
            return ResultBean.error(ErrorCodeEnum.FAIL, "加载失败: " + e.getMessage());
        }
    }

    /**
     * 从数据库获取所有工具
     */
    @GetMapping("/list")
    @ApiOperation(value = "获取所有工具", notes = "从数据库查询所有工具列表")
    public ResultBean<List<SQLTool>> getAllTools() {
        try {
            List<SQLTool> tools = toolService.loadToolsFromDatabase();
            return ResultBean.success(tools);
        } catch (Exception e) {
            return ResultBean.error(ErrorCodeEnum.FAIL, "查询失败: " + e.getMessage());
        }
    }

    /**
     * 从数据库获取启用的工具
     */
    @GetMapping("/enabled")
    @ApiOperation(value = "获取启用的工具", notes = "从数据库查询所有启用状态的工具")
    public ResultBean<List<SQLTool>> getEnabledTools() {
        try {
            List<SQLTool> tools = toolService.loadTotalEnabledToolsFromDatabase();
            return ResultBean.success(tools);
        } catch (Exception e) {
            return ResultBean.error(ErrorCodeEnum.FAIL, "查询失败: " + e.getMessage());
        }
    }
    @GetMapping("/enabled/{userId}")
    @ApiOperation(value = "获取用户启用的工具", notes = "从数据库查询指定用户所有启用状态的工具")
    public ResultBean<List<SQLTool>> getUserEnabledTools(@PathVariable String userId) {
        try {
            List<SQLTool> tools = toolService.loadEnabledToolsFromDatabase(userId);
            return ResultBean.success(tools);
        } catch (Exception e) {
            return ResultBean.error(ErrorCodeEnum.FAIL, "查询失败: " + e.getMessage());
        }
    }

    /**
     * 启用或禁用工具
     */
    @PutMapping("/{id}/toggle")
    @ApiOperation(value = "切换工具状态", notes = "启用或禁用指定的工具")
    public ResultBean<Void> toggleToolStatus(@PathVariable Integer id) {
        try {
            boolean success = toolService.toggleToolStatus(id);
            if (success) {
                return ResultBean.success("工具状态切换成功", null);
            } else {
                return ResultBean.error(ErrorCodeEnum.PARAM_ERROR, "工具不存在");
            }
        } catch (Exception e) {
            return ResultBean.error(ErrorCodeEnum.FAIL, "操作失败: " + e.getMessage());
        }
    }

    /**
     * 删除工具
     */
    @DeleteMapping("/{id}")
    @ApiOperation(value = "删除工具", notes = "从数据库删除指定的工具")
    public ResultBean<Void> deleteTool(@PathVariable Integer id) {
        try {
            boolean success = toolService.deleteTool(id);
            if (success) {
                return ResultBean.success("工具删除成功", null);
            } else {
                return ResultBean.error(ErrorCodeEnum.PARAM_ERROR, "工具不存在");
            }
        } catch (Exception e) {
            return ResultBean.error(ErrorCodeEnum.FAIL, "删除失败: " + e.getMessage());
        }
    }


    @GetMapping("/loadCallBack")
    @ApiOperation(value = "加载启用的工具回调", notes = "获取所有启用状态的工具并转换为 ToolCallback 数组")
    public ResultBean<Object> loadEnabledToolCallbacks() {
        try {
            Object callbacks = toolService.selectEnabledToolCallbacks();

            return ResultBean.success(JSON.toJSONString(callbacks));
        } catch (Exception e) {
            return ResultBean.error(ErrorCodeEnum.FAIL, "加载失败: " + e.getMessage());
        }
    }


}
