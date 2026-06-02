package com.nofirst.spring.tdd.zhihu.exception;

import com.nofirst.spring.tdd.zhihu.common.ResultCode;

public class UserNotExistedException extends ApiException {

    public UserNotExistedException() {
        super(ResultCode.FAILED, "user not exist");
    }
}
