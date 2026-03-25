package com.nofirst.spring.tdd.zhihu.unit.util;

import com.nofirst.spring.tdd.zhihu.util.EmailVerificationUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 邮箱验证码工具类测试
 */
public class EmailVerificationUtilTest {

    @Test
    void generateCode_should_generate_6_digit_code() {
        // When
        String code = EmailVerificationUtil.generateCode();

        // Then
        assertNotNull(code);
        assertEquals(6, code.length());
        assertTrue(code.matches("\\d{6}"), "验证码应该是 6 位数字");
    }

    @Test
    void generateCode_should_generate_different_codes() {
        // When
        String code1 = EmailVerificationUtil.generateCode();
        String code2 = EmailVerificationUtil.generateCode();

        // Then
        assertNotEquals(code1, code2, "多次生成的验证码应该不同");
    }

    @Test
    void generateToken_should_generate_signed_token() {
        // Given
        Integer userId = 1;
        String email = "test@example.com";
        String code = "123456";

        // When
        String token = EmailVerificationUtil.generateToken(userId, email, code);

        // Then
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void verifyToken_should_return_true_for_valid_token() {
        // Given
        Integer userId = 1;
        String email = "test@example.com";
        String code = "123456";
        String token = EmailVerificationUtil.generateToken(userId, email, code);

        // When
        EmailVerificationUtil.TokenPayload payload = EmailVerificationUtil.verifyToken(token);

        // Then
        assertNotNull(payload);
        assertEquals(userId, payload.getUserId());
        assertEquals(email, payload.getEmail());
        assertEquals(code, payload.getCode());
    }

    @Test
    void verifyToken_should_return_null_for_invalid_token() {
        // Given
        String invalidToken = "invalid.token.here";

        // When
        EmailVerificationUtil.TokenPayload payload = EmailVerificationUtil.verifyToken(invalidToken);

        // Then
        assertNull(payload);
    }

    @Test
    void verifyToken_should_return_null_for_tampered_token() {
        // Given
        Integer userId = 1;
        String email = "test@example.com";
        String code = "123456";
        String token = EmailVerificationUtil.generateToken(userId, email, code);
        String tamperedToken = token + "tampered";

        // When
        EmailVerificationUtil.TokenPayload payload = EmailVerificationUtil.verifyToken(tamperedToken);

        // Then
        assertNull(payload);
    }
}
