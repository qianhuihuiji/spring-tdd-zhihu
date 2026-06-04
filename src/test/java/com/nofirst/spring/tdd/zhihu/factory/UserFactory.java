package com.nofirst.spring.tdd.zhihu.factory;

import com.nofirst.spring.tdd.zhihu.mbg.model.User;
import com.nofirst.spring.tdd.zhihu.model.dto.UserRegisterDto;
import com.nofirst.spring.tdd.zhihu.security.AccountUser;
import net.datafaker.Faker;

import java.util.Date;

public class UserFactory extends EntityFactory<User> {

    private static final Faker faker = new Faker();

    private static String chinesePhone() {
        return faker.regexify("1[3-9]\\d{9}");
    }

    private boolean verified = true;

    public static UserFactory of() {
        return new UserFactory();
    }

    public UserFactory unverified() {
        this.verified = false;
        return this;
    }

    @Override
    protected User build() {
        User user = new User();
        user.setName(faker.name().username());
        user.setPhone(chinesePhone());
        user.setEmail(faker.internet().emailAddress());
        user.setPassword("password123");
        Date now = new Date();
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        user.setEmailVerifiedAt(verified ? now : null);
        return user;
    }

    public static User createUser() {
        return UserFactory.of().make();
    }

    public static AccountUser createAccountUser() {
        User user = createUser();
        user.setId(1);
        return new AccountUser(user.getId(), user.getName(), user.getPassword());
    }

    public static UserRegisterDto createUserRegisterDto() {
        UserRegisterDto dto = new UserRegisterDto();
        dto.setName(faker.name().username());
        dto.setPhone(chinesePhone());
        dto.setEmail(faker.internet().emailAddress());
        dto.setPassword("password123");
        return dto;
    }

    public static User createUnverifiedUser() {
        return UserFactory.of().unverified().make();
    }
}
