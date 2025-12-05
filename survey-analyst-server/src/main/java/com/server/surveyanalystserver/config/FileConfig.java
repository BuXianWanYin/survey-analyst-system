package com.server.surveyanalystserver.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 文件上传配置
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "file.upload")
public class FileConfig {

    /**
     * 文件上传路径
     */
    private String path;

    /**
     * 文件最大大小（字节）
     */
    private Long maxSize;
}

