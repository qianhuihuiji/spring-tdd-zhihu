package com.nofirst.spring.tdd.zhihu.listener;

import com.nofirst.spring.tdd.zhihu.component.EmailSender;
import com.nofirst.spring.tdd.zhihu.event.UserRegisteredEvent;
import com.nofirst.spring.tdd.zhihu.service.EmailVerificationService;
import com.nofirst.spring.tdd.zhihu.util.EmailVerificationUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 用户注册监听器测试
 */
public class UserRegisteredListenerTest {

    @Mock
    private EmailSender emailSender;

    @Mock
    private EmailVerificationService emailVerificationService;

    private UserRegisteredListener userRegisteredListener;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        userRegisteredListener = new UserRegisteredListener(emailSender, emailVerificationService);
    }

    @Test
    void onUserRegistered_should_send_verification_email() {
        // Given
        Integer userId = 1;
        String email = "test@example.com";
        UserRegisteredEvent event = new UserRegisteredEvent(this, userId, email);
        when(emailVerificationService.getVerificationCode(userId)).thenReturn("703887");

        // When
        userRegisteredListener.onUserRegistered(event);

        // Then
        verify(emailSender, times(1)).sendVerificationEmail(eq(email), anyString(), anyString());
    }

    @Test
    void onUserRegistered_should_save_verification_code() {
        // Given
        Integer userId = 1;
        String email = "test@example.com";
        UserRegisteredEvent event = new UserRegisteredEvent(this, userId, email);

        // When
        userRegisteredListener.onUserRegistered(event);

        // Then
        verify(emailVerificationService, times(1)).saveVerificationCode(eq(userId), eq(email), anyString());
    }
}
