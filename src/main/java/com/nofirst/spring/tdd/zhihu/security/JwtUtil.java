package com.nofirst.spring.tdd.zhihu.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class JwtUtil {

    public static final String HEADER = "Authorization";

    // JWT 密钥（生产环境需放在配置中心，加密存储）
    @Value("${jwt.secret:defaultSecretKey123456}")
    private String secret;
    // Token 过期时间（2 小时，单位：毫秒）
    @Value("${jwt.expire:7200000}")
    private Long expire;

    // 生成 JWT Token（携带用户 ID、角色信息）
    public String generateToken(Integer userId, String username) {
        // 1. 构建载荷信息
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        // 2. 生成 Token
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        Key key = Keys.hmacShaKeyFor(keyBytes);
        return Jwts.builder()
                .setClaims(claims) // 载荷信息
                .setIssuedAt(new Date()) // 签发时间
                .setExpiration(new Date(System.currentTimeMillis() + expire)) // 过期时间
                .signWith(key, SignatureAlgorithm.HS256) // 签名算法与密钥
                .compact();
    }

    // 解析 Token，获取载荷信息
    public Claims parseToken(String token) {
        try {
            byte[] keyBytes = Decoders.BASE64.decode(secret);
            Key key = Keys.hmacShaKeyFor(keyBytes);
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            log.error("Token 已过期", e);
            throw new RuntimeException("Token 已过期");
        } catch (UnsupportedJwtException | MalformedJwtException | SignatureException | IllegalArgumentException e) {
            log.error("Token 无效", e);
            throw new RuntimeException("Token 无效");
        }
    }

    public boolean isTokenExpired(Date expiration) {
        return expiration.before(new Date());
    }

    // 验证 Token 有效性（是否过期、签名是否正确）
    public boolean validateToken(String token) {
        try {
            byte[] keyBytes = Decoders.BASE64.decode(secret);
            Key key = Keys.hmacShaKeyFor(keyBytes);
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // 从 Token 中获取用户 ID
    public Integer getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        return (Integer) claims.get("userId");
    }

    // 从 Token 中获取用户角色
    public String getRoleFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get("role", String.class);
    }
}
