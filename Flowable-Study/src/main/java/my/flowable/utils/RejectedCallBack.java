package my.flowable.utils;

import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;

public class RejectedCallBack implements JavaDelegate {
    @Override
    public void execute(DelegateExecution execution) {
        System.out.println("Calling the external system for employee rejected "
                + execution.getVariable("employee"));

//      设置 瞬时变量
        execution.setTransientVariable("666","666");

    }
}
