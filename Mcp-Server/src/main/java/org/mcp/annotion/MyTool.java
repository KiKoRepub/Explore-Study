package org.mcp.annotion;

import org.springframework.context.annotation.Description;
import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.lang.annotation.*;

@Component
@Documented
@Description("")
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface MyTool {
    // 将这个 value 注解的值赋给 Description 注解的 value 属性
    @AliasFor(annotation = Description.class , attribute = "value")
    String value() default "";
}
