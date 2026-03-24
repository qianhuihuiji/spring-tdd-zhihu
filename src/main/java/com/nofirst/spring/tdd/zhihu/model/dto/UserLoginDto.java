package com.nofirst.spring.tdd.zhihu.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserLoginDto {

    @NotBlank
    private String username;

    @NotBlank
    private String password;
}
