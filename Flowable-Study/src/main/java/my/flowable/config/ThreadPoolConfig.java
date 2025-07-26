package my.flowable.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Data
@Configuration
public class ThreadPoolConfig {


    @Value("${threadPool.alive.second}")
    private int keepAliveSeconds;
    @Value("${threadPool.queue.capacity}")
    private int queueCapacity;
    @Bean
    public ThreadPoolTaskExecutor threadPoolTaskExecutor() {

            ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
            // 可用虚拟机的 最大数量
            int coreVirtualMachineNums = Runtime.getRuntime().availableProcessors();

            executor.setCorePoolSize(coreVirtualMachineNums);// 核心线程数
            executor.setMaxPoolSize(coreVirtualMachineNums * 2 + 1); // 最大线程数
            executor.setKeepAliveSeconds(keepAliveSeconds);
            executor.setQueueCapacity(queueCapacity);
            executor.setThreadNamePrefix("ThreadPoolExecutor-");
            // 设置拒绝策略
//            executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
//            executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
//            executor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardPolicy());
//            executor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardOldestPolicy());

        return executor;
    }

}
