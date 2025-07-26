package my.flowable;

import org.flowable.engine.*;
import org.flowable.engine.impl.cfg.StandaloneProcessEngineConfiguration;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class FlowableMain {

    public static void main(String[] args) {
//        ProcessEngineConfiguration cfg = new StandaloneProcessEngineConfiguration()
//                .setJdbcUrl("jdbc:mysql://localhost:3306/flowable?useUnicode=true&characterEncoding=UTF-8&useSSL=false&serverTimezone=Asia/Shanghai&nullCatalogMeansCurrent=true")
//                .setJdbcUsername("root")
//                .setJdbcPassword("123456")
//                .setJdbcDriver("com.mysql.cj.jdbc.Driver")
//                .setAsyncExecutorActivate(false)
//                .setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE);

        ProcessEngineConfiguration cfg = new StandaloneProcessEngineConfiguration()
                .setJdbcUrl("jdbc:h2:mem:flowable;DB_CLOSE_DELAY=-1")
                .setJdbcUsername("sa")
                .setJdbcPassword("")
                .setJdbcDriver("org.h2.Driver")
                .setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE);

        ProcessEngine processEngine = cfg.buildProcessEngine();


        // 获取 组件
        RepositoryService repositoryService = processEngine.getRepositoryService();
        RuntimeService runtimeService = processEngine.getRuntimeService();
        TaskService taskService = processEngine.getTaskService();


        // 获取部署对象
        Deployment deployment = repositoryService.createDeployment()
                .addClasspathResource("processes/myWorkStream2.bpmn20.xml")
                .deploy();
        // 获取 部署后的 流程定义对象 (保存了流程的各种信息，相当于是 流程的运行模版)
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                .deploymentId(deployment.getId())
                .singleResult();
        System.out.println("Found process definition : " + processDefinition.getName());

        // 让用户填入 流程变量 (请假人，请假天数，请假原因)
        Scanner scanner= new Scanner(System.in);

        System.out.println("Who are you?");
        String employee = scanner.nextLine();

        System.out.println("How many holidays do you want to request?");
        Integer nrOfHolidays = Integer.valueOf(scanner.nextLine());

        System.out.println("Why do you need them?");
        String description = scanner.nextLine();


        // 将 流程变量 存入Map,并在启动的时候放进 流程实例中
        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("employee", employee);
        variables.put("nrOfHolidays", nrOfHolidays);
        variables.put("description", description);
        ProcessInstance processInstance =
                runtimeService.startProcessInstanceByKey(
                        "myWorkStream-2",
                        variables);

        // 获得管理者的 待审核的任务 列表
        List<Task> tasks = taskService.createTaskQuery().taskCandidateGroup("managers").list();
        System.out.println("You have " + tasks.size() + " tasks:");
        for (int i=0; i<tasks.size(); i++) {
            System.out.println((i+1) + ") " + tasks.get(i).getName());
        }

        //询问需要处理 那个任务
        System.out.println("Which task would you like to complete?");
        int taskIndex = Integer.valueOf(scanner.nextLine());
        Task task = tasks.get(taskIndex - 1);
        Map<String, Object> processVariables = taskService.getVariables(task.getId());
        System.out.println(processVariables.get("employee") + " wants " +
                processVariables.get("nrOfHolidays") + " of holidays. Do you approve this?");


        // 处理逻辑 (在完成的时候 传入流程变量)
        boolean approved = scanner.nextLine().toLowerCase().equals("y");
        System.out.println(approved);
        variables = new HashMap<String, Object>();
        variables.put("approved", approved);
        System.out.println(variables);
//        runtimeService.setVariable(task.getExecutionId(),"approved", approved);
        taskService.complete(task.getId(), variables);

    }
}
