package my.flowable;


import junit.framework.Assert;
import org.apache.http.util.Asserts;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.test.Deployment;
import org.flowable.engine.test.FlowableRule;
import org.flowable.task.api.Task;
import org.junit.jupiter.api.Test;

public class FlowableTest {


    public FlowableRule flowableRule = new FlowableRule();

    @Test
    @Deployment
    public void deployTest() {
        RuntimeService runtimeService = flowableRule.getRuntimeService();
        runtimeService.startProcessInstanceByKey("myProcess");

        TaskService taskService = flowableRule.getTaskService();
        Task task = taskService.createTaskQuery().singleResult();
        Assert.assertEquals("My Task", task.getName());

        taskService.complete(task.getId());
        Assert.assertEquals(0, runtimeService.createProcessInstanceQuery().count());

    }
}
