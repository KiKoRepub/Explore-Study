package org.deepseek;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
//@MapperScan(basePackages = "org.deepseek.mapper")
/**
 * Hello world!
 *
 */
public class DeepSeekAiApplication {

    public static void main( String[] args ) {
        SpringApplication.run(DeepSeekAiApplication.class, args);
    }
}
