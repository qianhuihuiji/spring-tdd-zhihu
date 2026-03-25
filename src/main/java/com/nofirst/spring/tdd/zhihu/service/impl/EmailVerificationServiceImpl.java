package com.nofirst.spring.tdd.zhihu.service.impl;

import com.nofirst.spring.tdd.zhihu.mbg.mapper.EmailVerificationMapper;
import com.nofirst.spring.tdd.zhihu.mbg.model.EmailVerification;
import com.nofirst.spring.tdd.zhihu.service.EmailVerificationService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * 邮箱验证码服务实现类
 */
@AllArgsConstructor
@Service
public class EmailVerificationServiceImpl implements EmailVerificationService {

    private final EmailVerificationMapper emailVerificationMapper;

    @Override
    public void saveVerificationCode(Integer userId, String email, String code) {
        EmailVerification verification = new EmailVerification();
        verification.setUserId(userId);
        verification.setCode(code);
        verification.setEmail(email);
        verification.setCreatedAt(new Date());
        emailVerificationMapper.insertSelective(verification);
    }

    @Override
    public String getVerificationCode(Integer userId) {
        EmailVerification verification = emailVerificationMapper.selectByPrimaryKey(userId);
        if (verification == null) {
            return null;
        }
        return verification.getCode();
    }

    @Override
    public void markAsVerified(Integer userId) {
        EmailVerification verification = emailVerificationMapper.selectByPrimaryKey(userId);
        if (verification != null) {
            verification.setVerifiedAt(new Date());
            emailVerificationMapper.updateByPrimaryKeySelective(verification);
        }
    }
}
