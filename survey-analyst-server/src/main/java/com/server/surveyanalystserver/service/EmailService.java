package com.server.surveyanalystserver.service;

/**
 * 邮箱服务接口
 */
public interface EmailService {
    
    /**
     * 发送问卷提交通知邮件
     * @param toEmails 接收邮箱列表（多个邮箱用分号分隔）
     * @param surveyTitle 问卷标题
     * @param surveyId 问卷ID
     */
    void sendSurveySubmitNotification(String toEmails, String surveyTitle, Long surveyId);

    /**
     * 发送密码重置邮件
     * @param toEmail 接收邮箱
     * @param token 重置令牌
     */
    void sendPasswordResetEmail(String toEmail, String token);

    /**
     * 发送验证码邮件
     * @param toEmail 接收邮箱
     * @param code 验证码
     * @param type 类型（REGISTER-注册，RESET_PASSWORD-重置密码）
     */
    void sendVerificationCodeEmail(String toEmail, String code, String type);
}

