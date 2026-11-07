package com.howcode.aichat.bootstrap;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 六边形架构启动入口：只负责装配 core / infrastructure / transport。
 */
@MapperScan("com.howcode.aichat.infrastructure.persistence.mapper")
@SpringBootApplication(scanBasePackages = "com.howcode.aichat")
public class AiChatBootstrap {

    public static void main(String[] args) {
        SpringApplication.run(AiChatBootstrap.class, args);
    }
}
