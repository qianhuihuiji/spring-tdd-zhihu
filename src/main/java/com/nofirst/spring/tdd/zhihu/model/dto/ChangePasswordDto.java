package com.nofirst.spring.tdd.zhihu.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ChangePasswordDto {

    @NotBlank(message = "原密码不能为空")
    private String oldPassword;

    @Pattern(regexp = "^[a-zA-Z0-9_@#$!%*?&]{6,20}$",
            message = "密码必须是 6-20 位字母、数字或特殊字符")
    private String newPassword;
}
