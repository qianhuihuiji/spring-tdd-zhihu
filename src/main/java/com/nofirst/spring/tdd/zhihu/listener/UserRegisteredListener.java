package com.nofirst.spring.tdd.zhihu.listener;

import com.nofirst.spring.tdd.zhihu.component.EmailSender;
import com.nofirst.spring.tdd.zhihu.event.UserRegisteredEvent;
import com.nofirst.spring.tdd.zhihu.service.EmailVerificationService;
import com.nofirst.spring.tdd.zhihu.util.EmailVerificationUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 用户注册监听器 - 发送验证邮件
 */
@Slf4j
@Component
@AllArgsConstructor
@ConditionalOnProperty(prefix = "spring.mail", name = "host")
public class UserRegisteredListener {

    private final EmailSender emailSender;
    private final EmailVerificationService emailVerificationService;

    @EventListener
    public void onUserRegistered(UserRegisteredEvent event) {
        Integer userId = event.getUserId();
        String email = event.getEmail();

        // 生成验证码
        String code = EmailVerificationUtil.generateCode();

        // 保存验证码到数据库
        emailVerificationService.saveVerificationCode(userId, email, code);

        // 生成验证链接（实际应用中应该是完整 URL）
        String token = EmailVerificationUtil.generateToken(userId, email, code);
        String verificationLink = "http://localhost:8080/auth/verify-email?token=" + token;

        // 发送验证邮件
        emailSender.sendVerificationEmail(email, code, verificationLink);

        log.info("已为用户 {} 发送验证邮件到 {}", userId, email);
    }
}
