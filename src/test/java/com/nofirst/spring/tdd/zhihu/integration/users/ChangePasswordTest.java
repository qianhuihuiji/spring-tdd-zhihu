package com.nofirst.spring.tdd.zhihu.integration.users;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nofirst.spring.tdd.zhihu.common.ResultCode;
import com.nofirst.spring.tdd.zhihu.integration.BaseContainerTest;
import com.nofirst.spring.tdd.zhihu.mbg.mapper.UserMapper;
import com.nofirst.spring.tdd.zhihu.mbg.model.User;
import com.nofirst.spring.tdd.zhihu.model.dto.ChangePasswordDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithUserDetails;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ChangePasswordTest extends BaseContainerTest {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    public void setUp() {
        cleanUpUsersExceptDefault();
        // 重置 John (id=2) 的密码为 "password"，保证测试隔离
        User john = userMapper.selectByPrimaryKey(2);
        if (john != null) {
            john.setPassword(passwordEncoder.encode("password"));
            john.setUpdatedAt(new Date());
            userMapper.updateByPrimaryKeySelective(john);
        }
    }

    @Test
    void unauthenticated_user_can_not_change_password() throws Exception {
        ChangePasswordDto dto = new ChangePasswordDto();
        dto.setOldPassword("password");
        dto.setNewPassword("newPassword456");

        mockMvc.perform(post("/users/me/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().is(401));
    }

    @Test
    @WithUserDetails(value = "John", userDetailsServiceBeanName = "customUserDetailsService")
    void authenticated_user_can_change_password() throws Exception {
        // given
        ChangePasswordDto dto = new ChangePasswordDto();
        dto.setOldPassword("password");
        dto.setNewPassword("newPassword456");

        // when
        mockMvc.perform(post("/users/me/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ResultCode.SUCCESS.getCode()))
                .andExpect(jsonPath("$.data").value("密码修改成功"));

        // then - DB 已更新
        User updatedUser = userMapper.selectByPrimaryKey(2);
        assertThat(passwordEncoder.matches("newPassword456", updatedUser.getPassword())).isTrue();
        assertThat(passwordEncoder.matches("password", updatedUser.getPassword())).isFalse();
        assertThat(updatedUser.getUpdatedAt()).isNotNull();
    }

    @Test
    @WithUserDetails(value = "John", userDetailsServiceBeanName = "customUserDetailsService")
    void authenticated_user_can_not_change_password_with_wrong_old_password() throws Exception {
        ChangePasswordDto dto = new ChangePasswordDto();
        dto.setOldPassword("wrongOldPassword");
        dto.setNewPassword("newPassword456");

        mockMvc.perform(post("/users/me/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ResultCode.FAILED.getCode()))
                .andExpect(jsonPath("$.message").value("原密码不正确"));
    }

    @Test
    @WithUserDetails(value = "John", userDetailsServiceBeanName = "customUserDetailsService")
    void authenticated_user_can_not_change_password_with_invalid_new_password() throws Exception {
        ChangePasswordDto dto = new ChangePasswordDto();
        dto.setOldPassword("password");
        dto.setNewPassword("123");

        mockMvc.perform(post("/users/me/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ResultCode.VALIDATE_FAILED.getCode()))
                .andExpect(jsonPath("$.message").value("密码必须是 6-20 位字母、数字或特殊字符"));
    }

    @Test
    @WithUserDetails(value = "John", userDetailsServiceBeanName = "customUserDetailsService")
    void authenticated_user_can_not_change_password_with_blank_old_password() throws Exception {
        ChangePasswordDto dto = new ChangePasswordDto();
        dto.setOldPassword("");
        dto.setNewPassword("newPassword456");

        mockMvc.perform(post("/users/me/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ResultCode.VALIDATE_FAILED.getCode()))
                .andExpect(jsonPath("$.message").value("原密码不能为空"));
    }

    @Test
    @WithUserDetails(value = "John", userDetailsServiceBeanName = "customUserDetailsService")
    void authenticated_user_can_not_change_password_with_blank_new_password() throws Exception {
        ChangePasswordDto dto = new ChangePasswordDto();
        dto.setOldPassword("password");
        dto.setNewPassword("");

        mockMvc.perform(post("/users/me/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ResultCode.VALIDATE_FAILED.getCode()))
                .andExpect(jsonPath("$.message").value("密码必须是 6-20 位字母、数字或特殊字符"));
    }
}
