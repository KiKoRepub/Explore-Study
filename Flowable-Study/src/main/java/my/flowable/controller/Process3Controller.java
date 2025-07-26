package my.flowable.controller;

import lombok.RequiredArgsConstructor;
import my.flowable.service.UserService;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/process3")
public class Process3Controller extends BaseController {

    @Autowired
    private UserService userService;


    @PostMapping("/deploy")
    public ResponseEntity<String> createProcessDef() {

        Deployment deployment = repositoryService.createDeployment()
                .addClasspathResource("processes/myWorkStream3.bpmn20.xml")
                .name("登录流程")
                .deploy();

        ProcessDefinition definition = repositoryService.createProcessDefinitionQuery()
                .deploymentId(deployment.getId())
                .singleResult();
        String definitionId = definition.getId();
        System.out.println("流程定义ID：" + definitionId);
        return ResponseEntity.ok(definitionId);
    }

    @PostMapping("/start/{definitionId}")
    public ResponseEntity<String> startProcess(@PathVariable("definitionId") String definitionId,
                                               @RequestBody Map<String,Object> variableMap) {


        if (variableMap.get("messageStart") == null && variableMap.get("messageStart2") == null) {
            // 如果 两个都没有 就肯定不能开启流程 ，直接返回
            return ResponseEntity.badRequest().body("messageStart 和 messageStart2 不能同时为空");
        }




        // 如果不包含 用户信息 直接退出
        if (! variableMap.containsKey("userName") ||
            !variableMap.containsKey("password")) {

            return ResponseEntity.badRequest().body("用户名或密码不能为空");
        }

        ProcessInstance instance = null;
        if (variableMap.get("messageStart") != null){
            // 通过信息启动 (传入 messageName 不是 messageId)
          instance =  runtimeService.startProcessInstanceByMessage("startMessage",variableMap);
        }
        if (variableMap.get("messageStart2") != null){
            // 通过信息启动 (传入 messageName 不是 messageId)
          instance =  runtimeService.startProcessInstanceByMessage("startMessage2",variableMap);
        }


        // 启动流程
//        ProcessInstance instance = runtimeService.startProcessInstanceById(definitionId,variableMap);
        if (instance == null) return ResponseEntity.badRequest().body("启动失败");

        String processId = instance.getId();
//        runtimeService.setVariable();
//        runtimeService.setVariables(processId, variableMap);
        System.out.println("processId = " + processId);
        return ResponseEntity.ok(processId);
    }

    @PostMapping("/complete/{processId}")
    public ResponseEntity<String> completeTask(@PathVariable("processId") String processId,
                                               @RequestBody Map<String,Object> variableMap) {
        String userName = (String) runtimeService.getVariable(processId, "userName");
        // 获取当前需要执行的任务
        Task task = taskService.createTaskQuery()
                .taskAssignee(userName)
                .processInstanceId(processId).singleResult();

        taskService.complete(task.getId(), variableMap);

        return ResponseEntity.ok("任务完成");
    }
}
