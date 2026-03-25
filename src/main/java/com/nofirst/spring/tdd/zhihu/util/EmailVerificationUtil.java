package com.nofirst.spring.tdd.zhihu.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Random;

/**
 * 邮箱验证码工具类
 */
public class EmailVerificationUtil {

    private static final Random RANDOM = new Random();
    // JWT 密钥（用于签名验证 Token）
    private static final String SECRET_KEY = "EmailVerificationSecretKey2026ForTDD";

    /**
     * 生成 6 位数字验证码
     *
     * @return 6 位数字验证码
     */
    public static String generateCode() {
        // 生成 000000-999999 之间的随机数
        int code = RANDOM.nextInt(1000000);
        // 格式化为 6 位，不足补 0
        return String.format("%06d", code);
    }

    /**
     * 生成带签名的验证 Token
     *
     * @param userId 用户 ID
     * @param email  邮箱地址
     * @param code   验证码
     * @return 签名后的 Token
     */
    public static String generateToken(Integer userId, String email, String code) {
        SecretKey key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .claim("userId", userId)
                .claim("email", email)
                .claim("code", code)
                .signWith(key)
                .compact();
    }

    /**
     * 验证 Token 并返回载荷信息
     *
     * @param token 待验证的 Token
     * @return Token 载荷信息，验证失败返回 null
     */
    public static TokenPayload verifyToken(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            Integer userId = claims.get("userId", Integer.class);
            String email = claims.get("email", String.class);
            String code = claims.get("code", String.class);

            return new TokenPayload(userId, email, code);
        } catch (Exception e) {
            // Token 验证失败（签名无效、过期、格式错误等）
            return null;
        }
    }

    /**
     * Token 载荷信息
     */
    public static class TokenPayload {
        private final Integer userId;
        private final String email;
        private final String code;

        public TokenPayload(Integer userId, String email, String code) {
            this.userId = userId;
            this.email = email;
            this.code = code;
        }

        public Integer getUserId() {
            return userId;
        }

        public String getEmail() {
            return email;
        }

        public String getCode() {
            return code;
        }
    }
}
