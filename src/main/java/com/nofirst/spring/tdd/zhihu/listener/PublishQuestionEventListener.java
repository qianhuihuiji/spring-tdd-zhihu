package com.nofirst.spring.tdd.zhihu.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nofirst.spring.tdd.zhihu.event.PublishQuestionEvent;
import com.nofirst.spring.tdd.zhihu.mbg.mapper.ActivityMapper;
import com.nofirst.spring.tdd.zhihu.mbg.mapper.NotificationMapper;
import com.nofirst.spring.tdd.zhihu.mbg.mapper.UserMapper;
import com.nofirst.spring.tdd.zhihu.mbg.model.Activity;
import com.nofirst.spring.tdd.zhihu.mbg.model.Notification;
import com.nofirst.spring.tdd.zhihu.mbg.model.Question;
import com.nofirst.spring.tdd.zhihu.mbg.model.User;
import com.nofirst.spring.tdd.zhihu.mbg.model.UserExample;
import com.nofirst.spring.tdd.zhihu.util.InviteUserUtil;
import lombok.AllArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@AllArgsConstructor
public class PublishQuestionEventListener {

    private final NotificationMapper notificationMapper;
    private final UserMapper userMapper;
    private final ActivityMapper activityMapper;
    private final ObjectMapper objectMapper;


    @EventListener
    public void recordActivity(PublishQuestionEvent event) {
        Question question = event.getQuestion();
        Activity activity = new Activity();
        activity.setUserId(question.getUserId());
        activity.setType("published_question");
        activity.setSubjectId(question.getId());
        activity.setSubjectType(question.getClass().getSimpleName());
        Date now = new Date();
        activity.setCreatedAt(now);
        activity.setUpdatedAt(now);

        activityMapper.insert(activity);
    }

    @EventListener
    public void notifyInvitedUsers(PublishQuestionEvent event) {
        Question question = event.getQuestion();
        String data = getData(question);
        List<String> invitedUsers = InviteUserUtil.extractInvitedUser(question.getContent());
        if (CollectionUtils.isEmpty(invitedUsers)) {
            return;
        }
        invitedUsers.forEach(invitedUser -> {
            UserExample example = new UserExample();
            UserExample.Criteria criteria = example.createCriteria();
            criteria.andNameEqualTo(invitedUser);
            List<User> users = userMapper.selectByExample(example);
            if (!CollectionUtils.isEmpty(users)) {
                users.forEach(user -> {
                    Notification notification = new Notification();
                    notification.setType(PublishQuestionEvent.class.getName());
                    notification.setUserId(user.getId());
                    Date now = new Date();
                    notification.setCreatedAt(now);
                    notification.setUpdatedAt(now);
                    notification.setData(data);

                    notificationMapper.insert(notification);
                });
            }
        });
    }

    private String getData(Question question) {
        Map<String, String> data = new HashMap<>(2);
        data.put("title", question.getTitle());
        data.put("content", question.getContent());
        try {
            return objectMapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
