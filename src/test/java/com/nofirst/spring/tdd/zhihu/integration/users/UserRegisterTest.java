package com.nofirst.spring.tdd.zhihu.integration.users;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.icegreen.greenmail.store.FolderException;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.nofirst.spring.tdd.zhihu.common.ResultCode;
import com.nofirst.spring.tdd.zhihu.factory.UserFactory;
import com.nofirst.spring.tdd.zhihu.integration.BaseContainerTest;
import com.nofirst.spring.tdd.zhihu.mbg.mapper.EmailVerificationMapper;
import com.nofirst.spring.tdd.zhihu.mbg.mapper.UserMapper;
import com.nofirst.spring.tdd.zhihu.mbg.model.EmailVerification;
import com.nofirst.spring.tdd.zhihu.mbg.model.EmailVerificationExample;
import com.nofirst.spring.tdd.zhihu.mbg.model.User;
import com.nofirst.spring.tdd.zhihu.mbg.model.UserExample;
import com.nofirst.spring.tdd.zhihu.model.dto.UserRegisterDto;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserRegisterTest extends BaseContainerTest {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private EmailVerificationMapper emailVerificationMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    public void setupTestData() throws FolderException {
        cleanUpUsersExceptDefault();
        cleanUpEmailVerifications();
        greenMail.purgeEmailFromAllMailboxes();
    }

    @Test
    void guests_can_register_with_valid_credentials() throws Exception {
        // given
        UserRegisterDto registerDto = UserFactory.createUserRegisterDto();

        // when
        this.mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDto)))
                // then
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ResultCode.SUCCESS.getCode()))
                .andExpect(jsonPath("$.data").value("注册成功"));

        // 验证 1: 数据库中用户数据被正确插入
        UserExample userExample = new UserExample();
        userExample.createCriteria().andNameEqualTo(registerDto.getName());
        List<User> users = userMapper.selectByExample(userExample);
        assertThat(users).hasSize(1);

        User savedUser = users.get(0);
        assertThat(savedUser.getName()).isEqualTo(registerDto.getName());
        assertThat(savedUser.getPhone()).isEqualTo(registerDto.getPhone());
        assertThat(savedUser.getEmail()).isEqualTo(registerDto.getEmail());
        // 密码应该被 BCrypt 加密
        assertThat(passwordEncoder.matches(registerDto.getPassword(), savedUser.getPassword())).isTrue();
        // 创建时间应该被设置
        assertThat(savedUser.getCreatedAt()).isNotNull();
        assertThat(savedUser.getUpdatedAt()).isNotNull();

        // 验证 2: 验证码记录被创建
        EmailVerificationExample verificationExample = new EmailVerificationExample();
        verificationExample.createCriteria().andUserIdEqualTo(savedUser.getId());
        List<com.nofirst.spring.tdd.zhihu.mbg.model.EmailVerification> verifications =
                emailVerificationMapper.selectByExample(verificationExample);
        assertThat(verifications).hasSize(1);

        EmailVerification verification = verifications.get(0);
        assertThat(verification.getEmail()).isEqualTo(registerDto.getEmail());
        assertThat(verification.getCode()).hasSize(6); // 验证码应该是 6 位数字
        assertThat(verification.getCreatedAt()).isNotNull();
        // 验证时间应该为空（用户还未点击验证链接）
        assertThat(verification.getVerifiedAt()).isNull();

        // 验证 3: 验证邮件已发送到 GreenMail 测试服务器
        assertThat(greenMail.getReceivedMessages()).hasSize(1);
        MimeMessage receivedMessage = greenMail.getReceivedMessages()[0];
        assertThat(receivedMessage.getRecipients(MimeMessage.RecipientType.TO)[0].toString())
                .isEqualTo(registerDto.getEmail());
        assertThat(receivedMessage.getSubject()).contains("邮箱验证");
        // 验证邮件内容包含验证码
        // 邮件内容是 quoted-printable 编码的，但数字不会被编码，所以可以直接检查验证码
        String content = GreenMailUtil.getBody(receivedMessage);
        // 检查验证码（数字在 quoted-printable 中不会被编码）
        assertThat(content).contains(verification.getCode());
        // 检查邮件包含 HTML 结构（MIME 边界）
        assertThat(content).contains("Content-Type: text/html");
    }

    @Test
    void guests_can_not_register_with_duplicate_username() throws Exception {
        // given
        // 先创建一个用户
        User existingUser = UserFactory.createUser();
        userMapper.insertSelective(existingUser);

        UserRegisterDto registerDto = UserFactory.createUserRegisterDto();
        registerDto.setName(existingUser.getName());

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
        UserRegisterDto registerDto = UserFactory.createUserRegisterDto();
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
        UserRegisterDto registerDto = UserFactory.createUserRegisterDto();
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
        UserRegisterDto registerDto = UserFactory.createUserRegisterDto();
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
        UserRegisterDto registerDto = UserFactory.createUserRegisterDto();
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
}
