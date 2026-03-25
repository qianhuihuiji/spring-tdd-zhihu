package com.nofirst.spring.tdd.zhihu.unit.service;

import com.nofirst.spring.tdd.zhihu.mbg.mapper.EmailVerificationMapper;
import com.nofirst.spring.tdd.zhihu.mbg.model.EmailVerification;
import com.nofirst.spring.tdd.zhihu.service.EmailVerificationService;
import com.nofirst.spring.tdd.zhihu.service.impl.EmailVerificationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 邮箱验证码服务测试
 */
public class EmailVerificationServiceTest {

    @Mock
    private EmailVerificationMapper emailVerificationMapper;

    private EmailVerificationService emailVerificationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        emailVerificationService = new EmailVerificationServiceImpl(emailVerificationMapper);
    }

    @Test
    void saveVerificationCode_should_save_to_database() {
        // Given
        Integer userId = 1;
        String email = "test@example.com";
        String code = "123456";
        when(emailVerificationMapper.insertSelective(any(EmailVerification.class))).thenReturn(1);

        // When
        emailVerificationService.saveVerificationCode(userId, email, code);

        // Then
        verify(emailVerificationMapper, times(1)).insertSelective(any(EmailVerification.class));
    }

    @Test
    void getVerificationCode_should_return_saved_code() {
        // Given
        Integer userId = 1;
        EmailVerification verification = new EmailVerification();
        verification.setCode("123456");
        when(emailVerificationMapper.selectByPrimaryKey(userId)).thenReturn(verification);

        // When
        String code = emailVerificationService.getVerificationCode(userId);

        // Then
        assertEquals("123456", code);
    }

    @Test
    void getVerificationCode_should_return_null_when_not_found() {
        // Given
        Integer userId = 1;
        when(emailVerificationMapper.selectByPrimaryKey(userId)).thenReturn(null);

        // When
        String code = emailVerificationService.getVerificationCode(userId);

        // Then
        assertNull(code);
    }

    @Test
    void markAsVerified_should_update_verified_at() {
        // Given
        Integer userId = 1;
        EmailVerification verification = new EmailVerification();
        verification.setId(userId);
        when(emailVerificationMapper.selectByPrimaryKey(userId)).thenReturn(verification);
        when(emailVerificationMapper.updateByPrimaryKeySelective(any(EmailVerification.class))).thenReturn(1);

        // When
        emailVerificationService.markAsVerified(userId);

        // Then
        verify(emailVerificationMapper, times(1)).updateByPrimaryKeySelective(any(EmailVerification.class));
    }
}
