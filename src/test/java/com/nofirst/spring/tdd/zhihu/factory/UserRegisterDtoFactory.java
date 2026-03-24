package com.nofirst.spring.tdd.zhihu.factory;

import com.nofirst.spring.tdd.zhihu.model.dto.UserRegisterDto;

public class UserRegisterDtoFactory {

    public static UserRegisterDto createUserRegisterDto() {
        UserRegisterDto dto = new UserRegisterDto();
        dto.setName("testuser");
        dto.setPhone("13800138000");
        dto.setEmail("testuser@qq.com");
        dto.setPassword("password123");
        return dto;
    }

    public static UserRegisterDto createUserRegisterDtoWithName(String name) {
        UserRegisterDto dto = createUserRegisterDto();
        dto.setName(name);
        return dto;
    }
}
