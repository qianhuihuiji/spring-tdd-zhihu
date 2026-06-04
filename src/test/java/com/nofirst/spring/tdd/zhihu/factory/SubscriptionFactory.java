package com.nofirst.spring.tdd.zhihu.factory;


import com.nofirst.spring.tdd.zhihu.mbg.model.Subscription;

import java.util.Date;

public class SubscriptionFactory extends EntityFactory<Subscription> {

    private Integer userId = 1;
    private Integer questionId = 1;

    public static SubscriptionFactory of() {
        return new SubscriptionFactory();
    }

    public SubscriptionFactory byUser(Integer userId) {
        this.userId = userId;
        return this;
    }

    public SubscriptionFactory forQuestion(Integer questionId) {
        this.questionId = questionId;
        return this;
    }

    @Override
    protected Subscription build() {
        Subscription subscription = new Subscription();
        subscription.setUserId(userId);
        subscription.setQuestionId(questionId);
        Date now = new Date();
        subscription.setCreateTime(now);
        subscription.setUpdateTime(now);

        return subscription;
    }

    public static Subscription createSubscription(Integer userId, Integer questionId) {
        return SubscriptionFactory.of().byUser(userId).forQuestion(questionId).make();
    }
}
