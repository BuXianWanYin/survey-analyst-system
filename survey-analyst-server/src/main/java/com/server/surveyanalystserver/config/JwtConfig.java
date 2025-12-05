package com.server.surveyanalystserver.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "security.jwt")
public class JwtConfig {

    /**
     * JWT密钥
     */
    private String secret;

    /**
     * Token过期时间（毫秒）
     */
    private Long expiration;

    /**
     * 刷新Token过期时间（毫秒）
     */
    private Long refreshExpiration;
}

