package com.nofirst.spring.tdd.zhihu.factory;


import com.nofirst.spring.tdd.zhihu.mbg.model.User;
import com.nofirst.spring.tdd.zhihu.security.AccountUser;

public class UserFactory {

    public static User createUser() {
        return new User(1, "user", "password");
    }

    public static AccountUser createAccountUser() {
        User user = createUser();
        return new AccountUser(user.getId(), user.getName(), user.getPassword());
    }
}
