package my.flowable.event;

import my.flowable.annotion.MyEventHelper;
import org.flowable.engine.RuntimeService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 信号 只有被抛出后 才能被其他的 捕获到
 * 即 信号事件中
 *      至少需要一个 SignalThrowEvent
 *      可以有多个 SignalCatchEvent
 */
@MyEventHelper
public class SignalEventHelper {


    @Autowired
    private RuntimeService runtimeService;

    public void throwSignalEvent(String signalEventName,String executionId){
        // 抛出的信号 是单独的形式，只有 指定的流程 可以收到
        runtimeService.signalEventReceived(signalEventName, executionId);
    }

    public void throwSignalEvent(String signalEventName){
        // 抛出的信号 是广播 的形式，当前定义下的 所有流程(process) 都能收到
        runtimeService.signalEventReceived(signalEventName);
    }

}
