package com.nofirst.spring.tdd.zhihu.integration.users;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nofirst.spring.tdd.zhihu.common.ResultCode;
import com.nofirst.spring.tdd.zhihu.factory.UserRegisterDtoFactory;
import com.nofirst.spring.tdd.zhihu.integration.BaseContainerTest;
import com.nofirst.spring.tdd.zhihu.mbg.mapper.UserMapper;
import com.nofirst.spring.tdd.zhihu.mbg.model.User;
import com.nofirst.spring.tdd.zhihu.mbg.model.UserExample;
import com.nofirst.spring.tdd.zhihu.model.dto.UserLoginDto;
import com.nofirst.spring.tdd.zhihu.model.dto.UserRegisterDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserAuthTest extends BaseContainerTest {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    public void setupTestData() {
        // 只删除测试创建的用户（id > 3），保留初始化的 3 个用户（Jane、John、Foo）
        UserExample example = new UserExample();
        example.createCriteria().andIdGreaterThan(3);
        userMapper.deleteByExample(example);
    }

    @Test
    void guests_can_register_with_valid_credentials() throws Exception {
        // given
        UserRegisterDto registerDto = UserRegisterDtoFactory.createUserRegisterDto();

        // when
        this.mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDto)))
                // then
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ResultCode.SUCCESS.getCode()))
                .andExpect(jsonPath("$.data").value("注册成功"));
    }

    @Test
    void guests_can_not_register_with_duplicate_username() throws Exception {
        // given
        // 先创建一个用户
        User existingUser = new User();
        existingUser.setName("existingUser");
        existingUser.setPhone("13800138001");
        existingUser.setEmail("existing@qq.com");
        existingUser.setPassword(passwordEncoder.encode("password123"));
        existingUser.setCreatedAt(new Date());
        existingUser.setUpdatedAt(new Date());
        userMapper.insertSelective(existingUser);

        UserRegisterDto registerDto = UserRegisterDtoFactory.createUserRegisterDtoWithName("existingUser");

        // when & then
        this.mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ResultCode.FAILED.getCode()))
                .andExpect(jsonPath("$.message").value("用户名已存在"));
    }

    @Test
    void guests_can_not_register_with_blank_name() throws Exception {
        // given
        UserRegisterDto registerDto = UserRegisterDtoFactory.createUserRegisterDto();
        registerDto.setName("");

        // when
        this.mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDto)))
                // then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ResultCode.VALIDATE_FAILED.getCode()))
                .andExpect(jsonPath("$.message").value("用户名不能为空"));
    }

    @Test
    void guests_can_not_register_with_invalid_phone() throws Exception {
        // given
        UserRegisterDto registerDto = UserRegisterDtoFactory.createUserRegisterDto();
        registerDto.setPhone("12345678901");

        // when
        this.mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDto)))
                // then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ResultCode.VALIDATE_FAILED.getCode()))
                .andExpect(jsonPath("$.message").value("手机号格式不正确"));
    }

    @Test
    void guests_can_not_register_with_invalid_email() throws Exception {
        // given
        UserRegisterDto registerDto = UserRegisterDtoFactory.createUserRegisterDto();
        registerDto.setEmail("invalid-email");

        // when
        this.mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDto)))
                // then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ResultCode.VALIDATE_FAILED.getCode()))
                .andExpect(jsonPath("$.message").value("邮箱格式不正确"));
    }

    @Test
    void guests_can_not_register_with_invalid_password() throws Exception {
        // given
        UserRegisterDto registerDto = UserRegisterDtoFactory.createUserRegisterDto();
        registerDto.setPassword("123");

        // when
        this.mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDto)))
                // then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ResultCode.VALIDATE_FAILED.getCode()))
                .andExpect(jsonPath("$.message").value("密码必须是 6-20 位字母、数字或特殊字符"));
    }

    @Test
    void guests_can_login_with_correct_credentials() throws Exception {
        // given
        // 先创建一个用户（使用已知的密码）
        User user = new User();
        user.setName("loginUser");
        user.setPhone("13800138002");
        user.setEmail("loginuser@qq.com");
        user.setPassword(passwordEncoder.encode("password123"));
        user.setCreatedAt(new Date());
        user.setUpdatedAt(new Date());
        userMapper.insertSelective(user);

        UserLoginDto loginDto = new UserLoginDto();
        loginDto.setUsername("loginUser");
        loginDto.setPassword("password123");

        // when
        String jsonResponse = this.mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDto)))
                // then
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ResultCode.SUCCESS.getCode()))
                .andReturn()
                .getResponse()
                .getContentAsString();

        // 验证返回的是 JWT Token（非空字符串）
        String token = objectMapper.readTree(jsonResponse).get("data").asText();
        assertThat(token).isNotBlank();
        // JWT Token 格式：header.payload.signature，包含两个点
        assertThat(token).contains(".");
    }

    @Test
    void guests_can_not_login_with_nonexistent_username() throws Exception {
        // given
        UserLoginDto loginDto = new UserLoginDto();
        loginDto.setUsername("nonexistentUser");
        loginDto.setPassword("password123");

        // when & then
        this.mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDto)))
                .andDo(print())
                .andExpect(status().is(401));
    }

    @Test
    void guests_can_not_login_with_wrong_password() throws Exception {
        // given
        // 先创建一个用户
        User user = new User();
        user.setName("wrongPassUser");
        user.setPhone("13800138003");
        user.setEmail("wrongpass@qq.com");
        user.setPassword(passwordEncoder.encode("correctPassword"));
        user.setCreatedAt(new Date());
        user.setUpdatedAt(new Date());
        userMapper.insertSelective(user);

        UserLoginDto loginDto = new UserLoginDto();
        loginDto.setUsername("wrongPassUser");
        loginDto.setPassword("wrongPassword");

        // when & then
        this.mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDto)))
                .andDo(print())
                .andExpect(status().is(401));
    }

    @Test
    void guests_can_not_login_with_blank_username() throws Exception {
        // given
        UserLoginDto loginDto = new UserLoginDto();
        loginDto.setUsername("");
        loginDto.setPassword("password123");

        // when
        this.mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDto)))
                // then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ResultCode.VALIDATE_FAILED.getCode()));
    }

    @Test
    void guests_can_not_login_with_blank_password() throws Exception {
        // given
        UserLoginDto loginDto = new UserLoginDto();
        loginDto.setUsername("someUser");
        loginDto.setPassword("");

        // when
        this.mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDto)))
                // then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ResultCode.VALIDATE_FAILED.getCode()));
    }

    @Test
    void authenticated_users_can_logout() throws Exception {
        // given
        // 先登录获取 Token
        User user = new User();
        user.setName("logoutUser");
        user.setPhone("13800138004");
        user.setEmail("logoutuser@qq.com");
        user.setPassword(passwordEncoder.encode("password123"));
        user.setCreatedAt(new Date());
        user.setUpdatedAt(new Date());
        userMapper.insertSelective(user);

        UserLoginDto loginDto = new UserLoginDto();
        loginDto.setUsername("logoutUser");
        loginDto.setPassword("password123");

        String loginResponse = this.mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDto)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String token = objectMapper.readTree(loginResponse).get("data").asText();

        // when
        this.mockMvc.perform(get("/auth/logout")
                        .header("Authorization", token))
                // then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ResultCode.SUCCESS.getCode()))
                .andExpect(jsonPath("$.data").value("退出登录成功"));
    }
}
