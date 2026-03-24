package com.nofirst.spring.tdd.zhihu.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nofirst.spring.tdd.zhihu.event.PostAnswerEvent;
import com.nofirst.spring.tdd.zhihu.event.PublishQuestionEvent;
import com.nofirst.spring.tdd.zhihu.mbg.mapper.ActivityMapper;
import com.nofirst.spring.tdd.zhihu.mbg.mapper.NotificationMapper;
import com.nofirst.spring.tdd.zhihu.mbg.mapper.SubscriptionMapper;
import com.nofirst.spring.tdd.zhihu.mbg.mapper.UserMapper;
import com.nofirst.spring.tdd.zhihu.mbg.model.Activity;
import com.nofirst.spring.tdd.zhihu.mbg.model.Answer;
import com.nofirst.spring.tdd.zhihu.mbg.model.Notification;
import com.nofirst.spring.tdd.zhihu.mbg.model.Subscription;
import com.nofirst.spring.tdd.zhihu.mbg.model.SubscriptionExample;
import com.nofirst.spring.tdd.zhihu.mbg.model.User;
import lombok.AllArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
@AllArgsConstructor
public class PostAnswerEventListener {

    private final NotificationMapper notificationMapper;
    private final SubscriptionMapper subscriptionMapper;
    private final UserMapper userMapper;
    private final ActivityMapper activityMapper;
    private final ObjectMapper objectMapper;

    @EventListener
    public void recordActivity(PostAnswerEvent event) {
        Answer answer = event.getAnswer();
        Activity activity = new Activity();
        activity.setUserId(answer.getUserId());
        activity.setType("created_answer");
        activity.setSubjectId(answer.getId());
        activity.setSubjectType(answer.getClass().getSimpleName());
        Date now = new Date();
        activity.setCreatedAt(now);
        activity.setUpdatedAt(now);

        activityMapper.insert(activity);
    }

    @EventListener
    public void notifySubscribedUsers(PostAnswerEvent event) {
        Answer answer = event.getAnswer();
        SubscriptionExample subscriptionExample = new SubscriptionExample();
        subscriptionExample.createCriteria()
                .andQuestionIdEqualTo(answer.getQuestionId())
                .andUserIdNotEqualTo(answer.getUserId());
        List<Subscription> subscriptions = subscriptionMapper.selectByExample(subscriptionExample);
        String data = getData(answer);
        for (Subscription subscription : subscriptions) {
            User user = userMapper.selectByPrimaryKey(subscription.getUserId());
            if (Objects.nonNull(user)) {
                Notification notification = new Notification();
                notification.setType(PublishQuestionEvent.class.getName());
                notification.setUserId(user.getId());
                Date now = new Date();
                notification.setCreatedAt(now);
                notification.setUpdatedAt(now);
                notification.setData(data);

                notificationMapper.insert(notification);
            }
        }
    }

    private String getData(Answer answer) {
        Map<String, String> data = new HashMap<>(2);
        data.put("content", answer.getContent());
        try {
            return objectMapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
