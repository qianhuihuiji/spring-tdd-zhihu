package com.nofirst.spring.tdd.zhihu.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nofirst.spring.tdd.zhihu.event.PostCommentEvent;
import com.nofirst.spring.tdd.zhihu.mbg.mapper.NotificationMapper;
import com.nofirst.spring.tdd.zhihu.mbg.mapper.UserMapper;
import com.nofirst.spring.tdd.zhihu.mbg.model.Comment;
import com.nofirst.spring.tdd.zhihu.mbg.model.Notification;
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
public class PostCommentEventListener {

    private final NotificationMapper notificationMapper;
    private final UserMapper userMapper;
    private final ObjectMapper objectMapper;

    @EventListener
    public void notifyMentionedUsers(PostCommentEvent event) {
        Comment comment = event.getComment();
        String data = getData(comment);

        // 从评论内容中提取被@的用户
        List<String> mentionedUsers = InviteUserUtil.extractInvitedUser(comment.getContent());

        if (CollectionUtils.isEmpty(mentionedUsers)) {
            return;
        }

        mentionedUsers.forEach(mentionedUser -> {
            UserExample example = new UserExample();
            UserExample.Criteria criteria = example.createCriteria();
            criteria.andNameEqualTo(mentionedUser);
            List<User> users = userMapper.selectByExample(example);

            if (!CollectionUtils.isEmpty(users)) {
                users.forEach(user -> {
                    Notification notification = new Notification();
                    notification.setType(PostCommentEvent.class.getName());
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

    private String getData(Comment comment) {
        Map<String, String> data = new HashMap<>(2);
        data.put("content", comment.getContent());
        data.put("comment_id", String.valueOf(comment.getId()));
        try {
            return objectMapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
