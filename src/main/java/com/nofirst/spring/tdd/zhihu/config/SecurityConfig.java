package com.nofirst.spring.tdd.zhihu.config;

import com.nofirst.spring.tdd.zhihu.security.JwtAccessDeniedHandler;
import com.nofirst.spring.tdd.zhihu.security.JwtAuthenticationEntryPoint;
import com.nofirst.spring.tdd.zhihu.security.JwtAuthenticationFilter;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@AllArgsConstructor
@ComponentScan("com.nofirst.spring.tdd.zhihu.security")
public class SecurityConfig {

    private JwtAuthenticationFilter jwtAuthenticationFilter;
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private JwtAccessDeniedHandler jwtAccessDeniedHandler;

    // 密码加密器（BCrypt 加密，不可逆）
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 认证管理器（用于用户登录认证）
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    // 核心安全配置（接口授权、会话管理、过滤器顺序）
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 关闭 CSRF（前后端分离场景无需 CSRF 防护）
                .csrf(AbstractHttpConfigurer::disable)
                // 无状态会话（JWT 无状态认证，不创建 Session）
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 接口授权规则
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**").permitAll() // 登录接口放行
                        .requestMatchers(HttpMethod.GET, "/questions").permitAll() // 除了获取问题列表之外的接口，均不放行
                        .anyRequest().authenticated() // 其他接口需认证
                )// 异常处理器
                .exceptionHandling(
                        exception ->
                                exception.authenticationEntryPoint(jwtAuthenticationEntryPoint)
                                        .accessDeniedHandler(jwtAccessDeniedHandler))
                // 添加 JWT 过滤器（在用户名密码过滤器之前执行）
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}