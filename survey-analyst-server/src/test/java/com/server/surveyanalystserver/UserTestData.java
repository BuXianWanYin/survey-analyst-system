package com.server.surveyanalystserver;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 用户测试数据类
 * 用于生成密码的BCrypt加密结果
 */
public class UserTestData {

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * 生成密码的BCrypt加密结果
     * 可以修改此方法中的密码进行测试
     */
    @Test
    public void generatePasswordHash() {
        // 在这里修改要测试的密码
        String password = "123456";
        
        String encodedPassword = passwordEncoder.encode(password);
        
        System.out.println("========================================");
        System.out.println("密码加密结果:");
        System.out.println("----------------------------------------");
        System.out.println("原始密码: " + password);
        System.out.println("加密结果: " + encodedPassword);
        System.out.println("========================================");
    }
}

