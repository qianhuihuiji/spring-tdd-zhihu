package com.nofirst.spring.tdd.zhihu.component;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

/**
 * 邮件发送组件
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "spring.mail", name = "host")
public class EmailSender {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String fromEmail;

    public EmailSender(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * 发送验证邮件
     *
     * @param to    收件人邮箱
     * @param code  验证码
     * @param token 验证 Token
     */
    public void sendVerificationEmail(String to, String code, String token) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject("邮箱验证 - 知乎");

            // HTML 邮件内容
            String htmlContent = buildHtmlContent(code, token);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("验证邮件已发送到：{}", to);
        } catch (MessagingException e) {
            log.error("发送邮件失败：{}", to, e);
            throw new RuntimeException("发送邮件失败", e);
        }
    }

    /**
     * 构建 HTML 邮件内容
     */
    private String buildHtmlContent(String code, String token) {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<meta charset='UTF-8'>" +
                "<style>" +
                "body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }" +
                ".container { max-width: 600px; margin: 0 auto; padding: 20px; }" +
                ".header { background-color: #0084ff; color: white; padding: 20px; text-align: center; }" +
                ".content { padding: 30px 20px; background-color: #f9f9f9; }" +
                ".code { font-size: 24px; font-weight: bold; color: #0084ff; letter-spacing: 5px; }" +
                ".button { display: inline-block; padding: 12px 30px; background-color: #0084ff; color: white; text-decoration: none; border-radius: 5px; margin-top: 20px; }" +
                ".footer { text-align: center; padding: 20px; color: #999; font-size: 12px; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class='container'>" +
                "<div class='header'><h1>知乎</h1></div>" +
                "<div class='content'>" +
                "<h2>邮箱验证</h2>" +
                "<p>您好！</p>" +
                "<p>感谢您注册知乎。请使用以下验证码完成邮箱验证：</p>" +
                "<p class='code'>" + code + "</p>" +
                "<p>或者点击下面的按钮直接验证：</p>" +
                "<a href='" + token + "' class='button'>验证邮箱</a>" +
                "<p>验证码 10 分钟内有效。</p>" +
                "<p>如果不是您本人操作，请忽略此邮件。</p>" +
                "</div>" +
                "<div class='footer'>" +
                "<p>此邮件由系统自动发送，请勿回复</p>" +
                "<p>&copy; 2026 知乎</p>" +
                "</div>" +
                "</div>" +
                "</body>" +
                "</html>";
    }
}
