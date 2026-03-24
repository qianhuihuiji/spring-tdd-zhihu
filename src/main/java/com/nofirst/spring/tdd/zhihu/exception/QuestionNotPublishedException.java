package com.nofirst.spring.tdd.zhihu.exception;

import com.nofirst.spring.tdd.zhihu.common.ResultCode;

public class QuestionNotPublishedException extends ApiException {

    public QuestionNotPublishedException() {
        super(ResultCode.FAILED, "question not publish");
    }
}