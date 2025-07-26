package my.flowable.controller;

import my.flowable.event.SignalEventHelper;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/process4")
public class Process4Controller extends BaseController {


    @Autowired
    SignalEventHelper signalHelper;



    @PostMapping("/deploy")
    public ResponseEntity<String> createProcessDef() {

        Deployment deployment = repositoryService.createDeployment()
                .addClasspathResource("processes/myWorkStream4.bpmn20.xml")
                .name("测试流程")
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


        // 启动流程
        ProcessInstance instance = runtimeService.startProcessInstanceById(definitionId,variableMap);

        String processId = instance.getId();
//        runtimeService.setVariable();
//        runtimeService.setVariables(processId, variableMap);
        System.out.println("processId = " + processId);
        return ResponseEntity.ok(processId);
    }




}
