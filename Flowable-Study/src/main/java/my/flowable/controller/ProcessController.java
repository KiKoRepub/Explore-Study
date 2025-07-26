package my.flowable.controller;

import org.flowable.engine.HistoryService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/process")
public class ProcessController extends BaseController {



    @PostMapping("/deploy")
    public ResponseEntity<Object> createProcessDef() {

        Deployment deployment = repositoryService.createDeployment()
                //2.添加流程定义文件
                .addClasspathResource("process/myWorkStream1.bpmn20.xml")
                //3.设置流程名称
                .name("绩效流程")
                //4.部署
                .deploy();

        //2.通过流程部署id查询流程定义id
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                .deploymentId(deployment.getId()).singleResult();


//        myWorkStream-1:5:3d7cb0bb-6099-11f0-82ad-3c5576213735
        //5.返回部署的流程id
        return new ResponseEntity<>(processDefinition.getId(), HttpStatus.OK);
    }

    @PostMapping("/start")
    public ResponseEntity<Object> startProcess(@RequestParam(value = "processId") String processId,
                                               @RequestParam(value = "score", required = false, defaultValue = "10") Integer score,
                                               @RequestBody Map<String,Object> variables) {
//        可以前往 act_re_procdef 表中查看 流程定义的信息
        ProcessInstance instance = runtimeService.startProcessInstanceById(processId);
//             根据流程定义id，插入全局变量(会被存储在 act_ru_variable 中)
//         插入分数 (10分) 作为流程变量
        runtimeService.setVariable(instance.getId(), "score", score);
        // 插入其他的参数 (流程绑定的用户)
        variables.forEach((k,v)->{
            runtimeService.setVariable(processId,k,v);
        });

//        设置变量后 ，需要 通过 executionId 对变量进行调整( executionId == instanceId(全局变量) )

//        可以前往 act_ru_task 表中查看 流程的 运行情况

//        8d8a450d-5e32-11f0-b3e0-3c5576213735
//        266682b0-5e42-11f0-81f2-3c5576213735

//        65e9cfbc-6099-11f0-82ad-3c5576213735 <-> myWorkStream-1:5:3d7cb0bb-6099-11f0-82ad-3c5576213735
        return new ResponseEntity<>(instance.getId(), HttpStatus.OK);
//        runtimeService.startProcessInstanceByKey(processKey);


    }

    @PostMapping("/complete/{user}")
    public ResponseEntity<Object> completeTask(@PathVariable(value = "user") String user,
                                               @RequestParam(value = "processId", required = false) String processId,
                                               @RequestParam(value = "score", required = false, defaultValue = "10") Integer score) {
//        Flowable 不会进行 自定义用户的权限校验，
//        所以即使不通过这个方法获取任务id，而是直接填入 id (act_ru_task 表中的 id)
//        也可以直接完成步骤
        Task task = getTaskIdByUser(user);

//        对流程变量进行修改
        runtimeService.setVariable(processId,"score",score);
//        任务变量，当前任务中的变量，设置的时候 以 task中的 executionId 为 变量组的 Key
    // 如果需要 局部变量 要使用 setVariableLocal() 方法
//        executionId 和 processId 是一样的

        runtimeService.setVariable(task.getExecutionId(),"user",user);
//        可以前往 act_ru_task 表中查看 流程的 完成情况
//        每完成一项，数据库中保存的 流程会存储 下一步骤的信息
//        当全部步骤都完成了之后(到达了终止节点) act_ru_task 表中会删除该流程


        taskService.complete(task.getId());


        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/history")
    public String bindingHistory(@RequestParam("definitionId") String definitionId,
                                 @RequestParam("userKey")String userKey,
                                 @RequestParam("processId")String processId) {


        String currentTaskId = getCurrentTaskIdByDefinition(definitionId);
        Object o = runtimeService.getVariable(processId, userKey);

        return null;
    }



    private Task getTaskIdByUser(String user) {

//        根据 委托完成任务的用户 筛选出 有关的 所有流程任务 (目前只有一个 所以采用 singleResult )

        Task taskSingle = taskService.createTaskQuery()
                .taskAssignee(user)
//                .executionId(processId)
                .singleResult();

        List<Task> taskAll = taskService.createTaskQuery()
                .taskAssignee(user)
                .list();

//        String executionId = taskSingle.getExecutionId();
        return taskSingle;
    }


    private String getCurrentTaskIdByDefinition(String definitionId){
        Task taskSingle = taskService.createTaskQuery()
                .processDefinitionId(definitionId)
                .singleResult();

        System.out.println("taskSingle = " + taskSingle);
        return taskSingle.getId();
    }
}