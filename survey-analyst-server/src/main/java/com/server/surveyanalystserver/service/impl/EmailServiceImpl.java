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
import java.util.regex.Pattern;
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
    
    // 邮箱格式验证正则表达式
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );
    
    /**
     * 验证邮箱格式是否有效
     * @param email 邮箱地址
     * @return 是否有效
     */
    private boolean isValidEmail(String email) {
        if (!StringUtils.hasText(email)) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

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

            // 使用正确的数据页面路径格式：/survey/edit/data?id=xxx
            // 如果用户未登录，登录页面会自动添加redirect参数，登录成功后跳转回此页面
            String surveyLink = frontendUrl + "/survey/edit/data?id=" + surveyId;
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
                    // 验证邮箱格式
                    if (!isValidEmail(email)) {
                        log.warn("邮箱格式无效，跳过发送: {}", email);
                        continue;
                    }
                    
                    SimpleMailMessage message = new SimpleMailMessage();
                    message.setFrom(fromEmail);
                    message.setTo(email);
                    message.setSubject(subject);
                    message.setText(content);
                    
                    mailSender.send(message);
                    log.info("问卷提交通知邮件已发送到: {}", email);
                } catch (Exception e) {
                    // 详细记录错误信息
                    String errorMsg = e.getMessage();
                    if (e.getCause() != null) {
                        errorMsg += " (原因: " + e.getCause().getMessage() + ")";
                    }
                    log.error("发送邮件到 {} 失败: {}", email, errorMsg);
                    
                    // 如果是邮箱不存在或无效的错误，记录更详细的信息
                    if (errorMsg != null && (errorMsg.contains("550") || errorMsg.contains("non-existent") || errorMsg.contains("invalid"))) {
                        log.warn("邮箱 {} 可能不存在或无效，请检查邮箱地址是否正确", email);
                    }
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

    @Override
    public void sendVerificationCodeEmail(String toEmail, String code, String type) {
        if (mailSender == null) {
            log.warn("邮件服务未配置，跳过发送验证码邮件");
            return;
        }

        if (!StringUtils.hasText(toEmail)) {
            log.warn("接收邮箱为空，跳过发送验证码邮件");
            return;
        }

        try {
            String subject;
            String content;
            
            if ("REGISTER".equals(type)) {
                subject = "注册验证码 - 在线问卷调查与数据分析系统";
                content = String.format(
                        "您好！\n\n" +
                        "您正在注册账号，验证码为：\n\n" +
                        "%s\n\n" +
                        "验证码有效期为5分钟，请勿泄露给他人。\n\n" +
                        "如果您没有注册账号，请忽略此邮件。\n\n" +
                        "此邮件由系统自动发送，请勿回复。",
                        code
                );
            } else if ("RESET_PASSWORD".equals(type)) {
                subject = "密码重置验证码 - 在线问卷调查与数据分析系统";
                content = String.format(
                        "您好！\n\n" +
                        "您正在重置密码，验证码为：\n\n" +
                        "%s\n\n" +
                        "验证码有效期为5分钟，请勿泄露给他人。\n\n" +
                        "如果您没有申请重置密码，请忽略此邮件。\n\n" +
                        "此邮件由系统自动发送，请勿回复。",
                        code
                );
            } else {
                throw new RuntimeException("未知的验证码类型: " + type);
            }

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(content);
            
            mailSender.send(message);
            log.info("验证码邮件已发送到: {}, 类型: {}", toEmail, type);
        } catch (Exception e) {
            log.error("发送验证码邮件失败: {}", e.getMessage(), e);
            throw new RuntimeException("发送验证码邮件失败", e);
        }
    }
}

