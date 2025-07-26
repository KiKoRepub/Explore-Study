package my.flowable.delegete;

import my.flowable.annotion.MyDelegate;
import my.flowable.exception.MyFlowableException;
import org.flowable.common.engine.api.FlowableIllegalArgumentException;
import org.flowable.engine.delegate.BpmnError;

@MyDelegate
public class PrintTestLogDelegate {

    private static final String LOG_FORMAT = "————————————————%s——————————————————\n";

    public void printMessageStart2Received(){
        System.out.printf(LOG_FORMAT,"The start2Received message has been received");
    }

    public void printSignalLoginSuccess(){
        System.out.printf(LOG_FORMAT,"The login signal has been received");
    }

    public void printCycleLoginException(){
        System.out.printf(LOG_FORMAT,"The login cycle exception has occurred");
//        System.out.println("there will come to throw Error");
        try {
            Thread.sleep(1000);
            // 抛出异常
            // 只能有一个 事件可以接收这个错误并进行触发
            throw new BpmnError("500","login error");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void printExceptionLog(){
        System.out.printf(LOG_FORMAT,"The exception has occurred");
    }

    public void printEventGatewaySignalLog(){
        System.out.printf(LOG_FORMAT,"The event gateway signal has been received");
    }

    public void printSubExceptionLog(){
        System.out.printf(LOG_FORMAT,"The sub process's exception has occurred");
    }

    public void printSubSignalLog(){
        // 信号是全都可以收到
        System.out.printf(LOG_FORMAT,"The sub process's signal from process has been received");
    }


    public void printEndLog(){
        System.out.printf(LOG_FORMAT,"process is coming to end");
    }

}
