package com.nofirst.spring.tdd.zhihu.service;

import com.nofirst.spring.tdd.zhihu.security.AccountUser;

public interface QuestionSubscribeService {

    void subscribe(Integer questionId, AccountUser accountUser);

    void unsubscribe(Integer questionId, AccountUser accountUser);
}
