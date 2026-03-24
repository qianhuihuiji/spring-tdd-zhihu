package com.nofirst.spring.tdd.zhihu.service.impl;

import com.nofirst.spring.tdd.zhihu.mbg.mapper.SubscriptionMapper;
import com.nofirst.spring.tdd.zhihu.mbg.model.Subscription;
import com.nofirst.spring.tdd.zhihu.mbg.model.SubscriptionExample;
import com.nofirst.spring.tdd.zhihu.security.AccountUser;
import com.nofirst.spring.tdd.zhihu.service.QuestionSubscribeService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@AllArgsConstructor
public class QuestionSubscribeServiceImpl implements QuestionSubscribeService {

    private final SubscriptionMapper subscriptionMapper;

    @Override
    public void subscribe(Integer questionId, AccountUser accountUser) {
        Subscription subscription = new Subscription();
        subscription.setUserId(accountUser.getUserId());
        subscription.setQuestionId(questionId);
        Date now = new Date();
        subscription.setCreateTime(now);
        subscription.setUpdateTime(now);

        subscriptionMapper.insert(subscription);
    }

    @Override
    public void unsubscribe(Integer questionId, AccountUser accountUser) {
        SubscriptionExample example = new SubscriptionExample();
        SubscriptionExample.Criteria criteria = example.createCriteria();
        criteria.andIdEqualTo(accountUser.getUserId());
        criteria.andQuestionIdEqualTo(questionId);

        subscriptionMapper.deleteByExample(example);
    }
}
