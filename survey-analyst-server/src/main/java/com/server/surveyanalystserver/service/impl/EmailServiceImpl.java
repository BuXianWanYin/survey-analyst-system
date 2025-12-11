package com.server.surveyanalystserver.service.impl;

import com.server.surveyanalystserver.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 邮箱服务实现类
 */
@Slf4j
@Service
public class EmailServiceImpl implements EmailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String fromEmail;

    @Value("${survey.access.url}")
    private String frontendUrl;

    @Override
    public void sendSurveySubmitNotification(String toEmails, String surveyTitle, Long surveyId) {
        if (mailSender == null) {
            log.warn("邮件服务未配置，跳过发送邮件通知");
            return;
        }

        if (!StringUtils.hasText(toEmails)) {
            log.warn("接收邮箱为空，跳过发送邮件通知");
            return;
        }

        try {
            // 解析邮箱列表（支持分号分隔）
            List<String> emailList = Arrays.stream(toEmails.split(";"))
                    .map(String::trim)
                    .filter(StringUtils::hasText)
                    .collect(Collectors.toList());

            if (emailList.isEmpty()) {
                log.warn("邮箱列表为空，跳过发送邮件通知");
                return;
            }

            String surveyLink = frontendUrl + "/user/survey/data/" + surveyId;
            String subject = "问卷提交通知 - " + surveyTitle;
            String content = String.format(
                    "您好！\n\n" +
                    "您的问卷《%s》收到了新的提交。\n\n" +
                    "问卷链接：%s\n\n" +
                    "请及时查看问卷数据。\n\n" +
                    "此邮件由系统自动发送，请勿回复。",
                    surveyTitle, surveyLink
            );

            // 发送邮件给每个邮箱
            for (String email : emailList) {
                try {
                    SimpleMailMessage message = new SimpleMailMessage();
                    message.setFrom(fromEmail);
                    message.setTo(email);
                    message.setSubject(subject);
                    message.setText(content);
                    
                    mailSender.send(message);
                    log.info("问卷提交通知邮件已发送到: {}", email);
                } catch (Exception e) {
                    log.error("发送邮件到 {} 失败: {}", email, e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("发送问卷提交通知邮件失败: {}", e.getMessage(), e);
        }
    }

    @Override
    public void sendPasswordResetEmail(String toEmail, String token) {
        if (mailSender == null) {
            log.warn("邮件服务未配置，跳过发送密码重置邮件");
            return;
        }

        if (!StringUtils.hasText(toEmail)) {
            log.warn("接收邮箱为空，跳过发送密码重置邮件");
            return;
        }

        try {
            String resetLink = frontendUrl + "/auth/reset-password?token=" + token;
            String subject = "密码重置 - 在线问卷调查与数据分析系统";
            String content = String.format(
                    "您好！\n\n" +
                    "您正在申请重置密码。\n\n" +
                    "请点击以下链接重置您的密码（30分钟内有效）：\n" +
                    "%s\n\n" +
                    "如果您没有申请重置密码，请忽略此邮件。\n\n" +
                    "此邮件由系统自动发送，请勿回复。",
                    resetLink
            );

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(content);
            
            mailSender.send(message);
            log.info("密码重置邮件已发送到: {}", toEmail);
        } catch (Exception e) {
            log.error("发送密码重置邮件失败: {}", e.getMessage(), e);
            throw new RuntimeException("发送密码重置邮件失败", e);
        }
    }
}

