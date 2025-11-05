package org.dee.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.dee.service.ToolAuditService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.UUID;

/**
 * 工具调用审计切面
 * 自动记录所有带 @Tool 注解的方法调用
 */
@Aspect
@Component
public class ToolAuditAspect {

    @Autowired
    private ToolAuditService toolAuditService;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 环绕通知：拦截所有带 @Tool 注解的方法
     */
    @Around("@annotation(org.springframework.ai.tool.annotation.Tool)")
    public Object auditToolExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        // 获取方法信息
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Tool toolAnnotation = method.getAnnotation(Tool.class);
        
        String toolName = method.getDeclaringClass().getSimpleName();
        String methodName = method.getName();
        
        // 获取请求信息
        String conversationId = getConversationId();
        String requestId = UUID.randomUUID().toString();
        String userId = getUserId();
        String ipAddress = getIpAddress();
        
        // 序列化参数
        String parameters = serializeParameters(joinPoint.getArgs());
        
        // 记录工具调用开始
        Integer logId = toolAuditService.logToolStart(
                conversationId, requestId, toolName, methodName, 
                parameters, userId, ipAddress
        );
        
        try {
            // 执行目标方法
            Object result = joinPoint.proceed();
            
            // 记录成功
            String resultJson = serializeResult(result);
            toolAuditService.logToolSuccess(logId, resultJson);
            
            return result;
        } catch (Throwable throwable) {
            // 记录失败
            String errorMessage = throwable.getClass().getName() + ": " + throwable.getMessage();
            toolAuditService.logToolFailure(logId, errorMessage);
            
            // 重新抛出异常
            throw throwable;
        }
    }

    /**
     * 获取对话ID（从请求头或参数中）
     */
    private String getConversationId() {
        try {
            ServletRequestAttributes attributes = 
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String conversationId = request.getHeader("X-Conversation-Id");
                if (conversationId == null) {
                    conversationId = request.getParameter("conversationId");
                }
                return conversationId != null ? conversationId : "unknown";
            }
        } catch (Exception e) {
            // 忽略异常
        }
        return "unknown";
    }

    /**
     * 获取用户ID（从请求头或Session中）
     */
    private String getUserId() {
        try {
            ServletRequestAttributes attributes = 
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String userId = request.getHeader("X-User-Id");
                if (userId == null) {
                    userId = request.getParameter("userId");
                }
                return userId != null ? userId : "anonymous";
            }
        } catch (Exception e) {
            // 忽略异常
        }
        return "anonymous";
    }

    /**
     * 获取IP地址
     */
    private String getIpAddress() {
        try {
            ServletRequestAttributes attributes = 
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String ip = request.getHeader("X-Forwarded-For");
                if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                    ip = request.getHeader("X-Real-IP");
                }
                if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                    ip = request.getRemoteAddr();
                }
                return ip;
            }
        } catch (Exception e) {
            // 忽略异常
        }
        return "unknown";
    }

    /**
     * 序列化参数
     */
    private String serializeParameters(Object[] args) {
        try {
            if (args == null || args.length == 0) {
                return "[]";
            }
            return objectMapper.writeValueAsString(Arrays.asList(args));
        } catch (Exception e) {
            return "[序列化失败: " + e.getMessage() + "]";
        }
    }

    /**
     * 序列化结果
     */
    private String serializeResult(Object result) {
        try {
            if (result == null) {
                return "null";
            }
            return objectMapper.writeValueAsString(result);
        } catch (Exception e) {
            return "[序列化失败: " + e.getMessage() + "]";
        }
    }
}
