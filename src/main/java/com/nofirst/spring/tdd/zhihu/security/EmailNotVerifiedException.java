package com.nofirst.spring.tdd.zhihu.security;

import org.springframework.security.core.AuthenticationException;

/**
 * 邮箱未验证异常
 */
public class EmailNotVerifiedException extends AuthenticationException {

    public EmailNotVerifiedException(String msg) {
        super(msg);
    }
}
