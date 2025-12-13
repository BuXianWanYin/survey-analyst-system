package com.server.surveyanalystserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 在线问卷调查与数据分析系统启动类
 * Spring Boot应用程序入口，负责启动整个应用
 */
@SpringBootApplication
public class SurveyAnalystServerApplication {

    /**
     * 应用程序主入口
     * 启动Spring Boot应用，初始化Spring容器和所有配置
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(SurveyAnalystServerApplication.class, args);
    }

}
