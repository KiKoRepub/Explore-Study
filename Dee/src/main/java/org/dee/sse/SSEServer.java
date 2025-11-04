package org.dee.sse;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * SSE服务器，用于管理用户的SSE连接
 */
@Slf4j
public class SSEServer {


    private static final Map<String, SseEmitter> userEmitters = new ConcurrentHashMap<>();


    public static boolean connect(String userId) {
        try {
            // timeout 设置为0，表示永不超时,
            SseEmitter emitter = new SseEmitter(0L);

            emitter.onTimeout(onTimeoutCallback(userId));
            emitter.onError(onErrorCallback(userId));
            emitter.onCompletion(onCompletionCallback(userId));

            userEmitters.put(userId, emitter);

            return true;
        } catch (Exception e) {
            log.error("SSE连接失败: {}", e.getMessage());
            return false;
        }
    }

    private static Runnable onTimeoutCallback(String userId){
        return () -> {
            log.info("SSE连接超时");
            removeEmitter(userId);
        };
    }
    private static Consumer<Throwable> onErrorCallback(String userId){
        return (e) -> {
            log.info("SSE连接错误: {}", e.getMessage());
            removeEmitter(userId);
        };
    }
    private static Runnable onCompletionCallback(String userId){
        return () -> {
            log.info("SSE连接完成");
            removeEmitter(userId);
        };
    }

    public static boolean removeEmitter(String userId){
        return userEmitters.remove(userId) != null;
    }
}
