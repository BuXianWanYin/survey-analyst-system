package com.server.surveyanalystserver.service;

/**
 * 邮箱服务接口
 */
public interface EmailService {
    
    /**
     * 发送问卷提交通知邮件
     * 当有用户提交问卷时，向指定邮箱发送通知邮件
     * @param toEmails 接收邮箱列表（多个邮箱用分号分隔）
     * @param surveyTitle 问卷标题，用于邮件内容
     * @param surveyId 问卷ID，用于生成查看链接
     */
    void sendSurveySubmitNotification(String toEmails, String surveyTitle, Long surveyId);

    /**
     * 发送密码重置邮件
     * 向用户邮箱发送密码重置链接，包含重置令牌
     * @param toEmail 接收邮箱
     * @param token 密码重置令牌，用于验证身份
     */
    void sendPasswordResetEmail(String toEmail, String token);

    /**
     * 发送验证码邮件
     * 向用户邮箱发送验证码，用于注册或重置密码
     * @param toEmail 接收邮箱
     * @param code 验证码
     * @param type 验证码类型（REGISTER-注册，RESET_PASSWORD-重置密码）
     */
    void sendVerificationCodeEmail(String toEmail, String code, String type);
}

