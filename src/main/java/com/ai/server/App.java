package com.ai.server;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * springboot启动类
 */
@SpringBootApplication
@MapperScan("com.ai.server.mapper")
@Slf4j
public class App {
    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
        log.info("项目启动成功");
        log.info("项目启动:http://127.0.0.1:8080/");
        log.info("Swagger UI 界面：<http://localhost:8080/swagger-ui/index.html");
    }
}
