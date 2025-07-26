package my.flowable.utils;

import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;

public class ApprovedCallBack implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) {
        System.out.println("Calling the external system for employee approved "
                + execution.getVariable("employee"));
    }
}
