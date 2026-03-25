package com.nofirst.spring.tdd.zhihu.factory;


import com.nofirst.spring.tdd.zhihu.mbg.model.User;
import com.nofirst.spring.tdd.zhihu.model.dto.UserRegisterDto;
import com.nofirst.spring.tdd.zhihu.security.AccountUser;

import java.util.Date;

public class UserFactory {

    public static User createUser() {
        User user = new User();
        user.setName("verifiedUser");
        user.setPhone("13800138000");
        user.setEmail("verified@example.com");
        user.setPassword("password123");
        Date now = new Date();
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        user.setEmailVerifiedAt(now);
        return user;
    }

    public static AccountUser createAccountUser() {
        User user = createUser();
        user.setId(1);
        return new AccountUser(user.getId(), user.getName(), user.getPassword());
    }

    public static UserRegisterDto createUserRegisterDto() {
        UserRegisterDto dto = new UserRegisterDto();
        dto.setName("testuser");
        dto.setPhone("13800138000");
        dto.setEmail("testuser@qq.com");
        dto.setPassword("password123");
        return dto;
    }

    public static User createUnverifiedUser() {
        User user = createUser();
        user.setEmailVerifiedAt(null);
        return user;
    }

}
