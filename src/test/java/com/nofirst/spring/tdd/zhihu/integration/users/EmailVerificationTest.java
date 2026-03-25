package com.nofirst.spring.tdd.zhihu.integration.users;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nofirst.spring.tdd.zhihu.factory.UserFactory;
import com.nofirst.spring.tdd.zhihu.integration.BaseContainerTest;
import com.nofirst.spring.tdd.zhihu.mbg.mapper.UserMapper;
import com.nofirst.spring.tdd.zhihu.mbg.model.User;
import com.nofirst.spring.tdd.zhihu.model.dto.UserLoginDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 用户登录邮箱验证集成测试
 */
class EmailVerificationTest extends BaseContainerTest {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        cleanUpUsersExceptDefault();
    }

    @Test
    void login_should_fail_when_email_verified_at_is_null() throws Exception {
        // Given
        User user = UserFactory.createUnverifiedUser();
        user.setPassword(passwordEncoder.encode("password123"));
        userMapper.insertSelective(user);

        // When & Then
        UserLoginDto loginDto = new UserLoginDto();
        loginDto.setUsername(user.getName());
        loginDto.setPassword("password123");
        mockMvc.perform(MockMvcRequestBuilders.post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.message").value("请先验证邮箱"));
    }

    @Test
    void login_should_success_when_email_verified_at_is_not_null() throws Exception {
        // Given
        User user = UserFactory.createUser();
        user.setPassword(passwordEncoder.encode("password123"));
        userMapper.insertSelective(user);

        // When & Then
        UserLoginDto loginDto = new UserLoginDto();
        loginDto.setUsername(user.getName());
        loginDto.setPassword("password123");
        mockMvc.perform(MockMvcRequestBuilders.post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}
