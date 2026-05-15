package com.campus2hand.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String senderEmail;

    @Value("${spring.mail.nickname:Campus2Hand}")
    private String senderNickname;

    @Async
    public void sendVerifyCode(String toEmail, String code) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(formatSender(senderEmail, senderNickname));
            message.setTo(toEmail);
            message.setSubject("[Campus2Hand] 邮箱验证码");
            message.setText(buildVerifyCodeContent(code));
            mailSender.send(message);
            log.info("Verify code email sent to: {}", maskEmail(toEmail));
        } catch (Exception e) {
            log.error("Failed to send verify code email to {}", maskEmail(toEmail), e);
            throw new RuntimeException("邮件发送失败", e);
        }
    }

    @Async
    public void sendVerifyEmail(String toEmail, String token) {
        try {
            String verifyUrl = buildVerifyUrl(token);
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(formatSender(senderEmail, senderNickname));
            message.setTo(toEmail);
            message.setSubject("[Campus2Hand] 邮箱验证");
            message.setText(buildVerifyEmailContent(verifyUrl));
            mailSender.send(message);
            log.info("Verify email sent to: {}", maskEmail(toEmail));
        } catch (Exception e) {
            log.error("Failed to send verify email to {}", maskEmail(toEmail), e);
            throw new RuntimeException("邮件发送失败", e);
        }
    }

    @Async
    public void sendPasswordResetEmail(String toEmail, String token) {
        try {
            String resetUrl = buildResetPasswordUrl(token);
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(formatSender(senderEmail, senderNickname));
            message.setTo(toEmail);
            message.setSubject("[Campus2Hand] 密码重置");
            message.setText(buildPasswordResetContent(resetUrl));
            mailSender.send(message);
            log.info("Password reset email sent to: {}", maskEmail(toEmail));
        } catch (Exception e) {
            log.error("Failed to send password reset email to {}", maskEmail(toEmail), e);
            throw new RuntimeException("邮件发送失败", e);
        }
    }

    private String formatSender(String email, String nickname) {
        if (nickname != null && !nickname.isEmpty()) {
            return nickname + " <" + email + ">";
        }
        return email;
    }

    private String buildVerifyCodeContent(String code) {
        return String.format("您的 Campus2Hand 邮箱验证码是：%s\n\n该验证码有效期为5分钟，请尽快使用。\n\n如非本人操作，请忽略此邮件。", code);
    }

    private String buildVerifyEmailContent(String verifyUrl) {
        return String.format("欢迎注册 Campus2Hand！\n\n请点击以下链接完成邮箱验证：\n%s\n\n该链接有效期为24小时。\n\n如非本人操作，请忽略此邮件。", verifyUrl);
    }

    private String buildPasswordResetContent(String resetUrl) {
        return String.format("您正在重置 Campus2Hand 账户密码。\n\n请点击以下链接完成密码重置：\n%s\n\n该链接有效期为1小时。\n\n如非本人操作，请忽略此邮件。", resetUrl);
    }

    private String buildVerifyUrl(String token) {
        return "https://campus2hand.com/api/v1/auth/verify-email?token=" + token;
    }

    private String buildResetPasswordUrl(String token) {
        return "https://campus2hand.com/api/v1/auth/reset-password?token=" + token;
    }

    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return email;
        }
        int atIndex = email.indexOf('@');
        String username = email.substring(0, atIndex);
        String domain = email.substring(atIndex);
        if (username.length() <= 2) {
            return username.charAt(0) + "*" + domain;
        }
        return username.charAt(0) + "****" + username.charAt(username.length() - 1) + domain;
    }
}