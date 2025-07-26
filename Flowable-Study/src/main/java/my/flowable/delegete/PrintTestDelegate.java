package my.flowable.delegete;

import my.flowable.annotion.MyDelegate;
import org.flowable.common.engine.api.FlowableIllegalArgumentException;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;

@MyDelegate
public class PrintTestDelegate implements JavaDelegate {
    @Override
    public void execute(DelegateExecution execution) {
        System.out.println("PrintTestDelegate");
        Object hasLogin = execution.getVariable("hasLogin");
        // 登录成功后 再次进入这里 肯定是需要接收信号的情况
        if (hasLogin instanceof  Boolean logined) {
            if (logined) {
                execution.setVariable("hasSignal",true);
                System.out.println("信号已经收到了_______________________________________");
            }else {

            }
        }


    }
}
