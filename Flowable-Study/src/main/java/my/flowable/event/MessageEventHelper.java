package my.flowable.event;

import my.flowable.annotion.MyEventHelper;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.Execution;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;

@MyEventHelper
public class MessageEventHelper {

    @Autowired
    RuntimeService runtimeService;

    @Autowired
    RepositoryService repositoryService;

    // 使用消息创建 流程实例 的方法
    public void startProcessInstanceByMessage(String messageName){
        runtimeService.startProcessInstanceByMessage(messageName);
    }
    public void startProcessInstanceByMessage(String messageName, Map<String,Object> processVariables){
        runtimeService.startProcessInstanceByMessage(messageName, processVariables);
    }
    public void startProcessInstanceByMessage(String messageName,String businessKey, Map<String,Object> processVariables){
        runtimeService.startProcessInstanceByMessage(messageName, businessKey, processVariables);
    }

//  触发消息的 抛出事件
    public void messageEventReceived(String messageName, String executionId){
        runtimeService.messageEventReceived(messageName, executionId);
    }
    public void messageEventReceived(String messageName, String executionId, HashMap<String, Object> processVariables){
        runtimeService.messageEventReceived(messageName, executionId, processVariables);
    }

    public ProcessDefinition queryMessageProcessDefinition(String messageName){
        // 流程定义中 消息订阅出现在第一个开始节点时
        // 由于 消息只会与 一个流程定义 进行绑定，所以结果为 0 or 1 个 流程定义。
        // 即使流程定义更新了，这种绑定关系没变的情况下，始终就只会返回 最新的流程定义
        return repositoryService.createProcessDefinitionQuery()
                .messageEventSubscriptionName(messageName)
                .singleResult();
    }

    public Execution queryMessageExecution(String messageName,String orderId){
        // 对于 中间捕获的 流程来讲，订阅情况 和 execution 有关
        // 查询的 variable 属于 关联查询 部分，对于不同流程可能会出现变化
        return runtimeService.createExecutionQuery()
                .messageEventSubscriptionName(messageName)
                .variableValueEquals("orderId", orderId)
                .singleResult();
    }


}
