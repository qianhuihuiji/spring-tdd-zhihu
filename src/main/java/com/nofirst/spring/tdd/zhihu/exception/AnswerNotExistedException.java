package com.nofirst.spring.tdd.zhihu.exception;

import com.nofirst.spring.tdd.zhihu.common.ResultCode;

public class AnswerNotExistedException extends ApiException {

    public AnswerNotExistedException() {
        super(ResultCode.FAILED, "answer not exist");
    }
}
