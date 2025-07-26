package my.flowable.controller;

import org.flowable.engine.HistoryService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.TaskService;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.variable.api.history.HistoricVariableInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/history")
public class HistoryController extends BaseController{

    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private HistoryService historyService;
    @Autowired
    private TaskService taskService;


    @GetMapping("/deploy")
    public String deployHistoryProcess(){

        Deployment deployment = repositoryService.createDeployment()
                .addClasspathResource("processes/historyProcess.bpmn20.xml")
                .name("historyProcess")
                .deploy();

        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                .deploymentId(deployment.getId())
                .singleResult();

        return "History Process deployed with id: " + processDefinition.getId();
    }


    /**
     * 查询指定用户的历史绩效.
     *
     * @param userId 用户id
     * @return 历史流程集合
     */
    @PostMapping("/userId/{userId}")
    public ResponseEntity<Object> getHistoryProcessDef(@PathVariable String userId) {

        //1.根据用户id查询历史流程
//        可以在 act_hi_procinst历史流程表 中查找到 记录
//        可以在 act_hi_varinst 历史变量表 中查找到 记录

        // 在 varinst 中查找记录,找出包含 user 变量的 记录，由于设定的是 全局变量，所以返回的是InstanceId
//        如果 设定的是 局部变量，则会返回 TaskId
        List<HistoricVariableInstance> historicVariableInstanceList = historyService
                .createHistoricVariableInstanceQuery()
                .variableValueEquals("user", userId)
                .list();
        //2.抽出变量对应的 流程ID
        Set<String> processInstanceIds = historicVariableInstanceList.stream()
                .map(HistoricVariableInstance::getProcessInstanceId)
                .collect(Collectors.toSet());

        //3.根据 流程ID 查询历史流程
        // 如果前面获取的是 TaskId，则这里需要
        // .createHistoricTaskInstanceQuery()
        List<HistoricProcessInstance> historicProcessInstanceList = historyService
                .createHistoricProcessInstanceQuery()
                .processInstanceIds(processInstanceIds)
                .list();
        //4.抽出流程定义ID集合
        Set<String> processDefinitionIds = historicProcessInstanceList.stream()
                .map(HistoricProcessInstance::getProcessDefinitionId)
                .collect(Collectors.toSet());
        //5.根据流程定义ID获取流程定义
        List<ProcessDefinition> processDefinitionList = repositoryService
                .createProcessDefinitionQuery()
                .processDefinitionIds(processDefinitionIds)
                .list();

        //6.抽出流程部署id
        Set<String> deploymentIds = processDefinitionList.stream()
                .map(ProcessDefinition::getDeploymentId)
                .collect(Collectors.toSet());
        //7.返回流程部署id

        List<HistoryInfoVo> historyList = new ArrayList<>();



        return new ResponseEntity<>(deploymentIds, HttpStatus.OK);
    }
}
