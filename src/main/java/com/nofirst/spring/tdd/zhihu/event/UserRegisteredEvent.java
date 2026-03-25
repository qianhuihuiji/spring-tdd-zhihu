package com.nofirst.spring.tdd.zhihu.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 用户注册成功事件
 */
public class UserRegisteredEvent extends ApplicationEvent {

    @Getter
    private final Integer userId;

    @Getter
    private final String email;

    public UserRegisteredEvent(Object source, Integer userId, String email) {
        super(source);
        this.userId = userId;
        this.email = email;
    }
}
