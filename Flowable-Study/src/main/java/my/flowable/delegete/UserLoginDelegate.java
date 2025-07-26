package my.flowable.delegete;

import lombok.experimental.Delegate;
import my.flowable.annotion.MyDelegate;
import my.flowable.service.UserService;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;

@MyDelegate("userTask-1")
public class UserLoginDelegate implements JavaDelegate {

    @Autowired
    private UserService userService;

    @Override
    public void execute(DelegateExecution execution) {
        try {
//            definition id -> 96e162a8-6854-11f0-a214-3c5576213735
//            process id -> 9d0c365a-6854-11f0-a214-3c5576213735
            System.out.println("processId in delegate is = " + execution.getId());
            String username = (String) execution.getVariable("userName");
//            String token  = (String) execution.getVariable("token");
            String password = (String) execution.getVariable("password");


//                        userService.login(username,password);
            if (username.equals("admin") && password.equals("123456")) {

                // 登录成功 执行 定时任务 和 信号发布
                execution.setVariable("hasLogin",true);
                // 延迟 10秒 后执行
                execution.setVariable("durationTimerInterval","PT10S");



                System.out.println("user login success");
            }else {
                // 登录失败 执行循环任务
                execution.setVariable("hasLogin",false);
                // 每 10秒 触发一次
                execution.setVariable("recurringInterval","R/PT10S");
                System.out.println("user login fail");
//                execution.setActive(false);
            }

        }catch (Exception e){
            e.printStackTrace();
            System.out.println("error");
        }
    }
}
