package com.nofirst.spring.tdd.zhihu.exception;

import com.nofirst.spring.tdd.zhihu.common.ResultCode;

public class QuestionNotExistedException extends ApiException {

    public QuestionNotExistedException() {
        super(ResultCode.FAILED, "question not exist");
    }
}