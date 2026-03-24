package com.nofirst.spring.tdd.zhihu.integration.users;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.PageInfo;
import com.nofirst.spring.tdd.zhihu.common.CommonResult;
import com.nofirst.spring.tdd.zhihu.common.ResultCode;
import com.nofirst.spring.tdd.zhihu.factory.AnswerFactory;
import com.nofirst.spring.tdd.zhihu.factory.QuestionFactory;
import com.nofirst.spring.tdd.zhihu.factory.SubscriptionFactory;
import com.nofirst.spring.tdd.zhihu.integration.BaseContainerTest;
import com.nofirst.spring.tdd.zhihu.mbg.mapper.NotificationMapper;
import com.nofirst.spring.tdd.zhihu.mbg.mapper.QuestionMapper;
import com.nofirst.spring.tdd.zhihu.mbg.mapper.SubscriptionMapper;
import com.nofirst.spring.tdd.zhihu.mbg.model.NotificationExample;
import com.nofirst.spring.tdd.zhihu.mbg.model.Question;
import com.nofirst.spring.tdd.zhihu.mbg.model.Subscription;
import com.nofirst.spring.tdd.zhihu.model.dto.AnswerDto;
import com.nofirst.spring.tdd.zhihu.model.vo.NotificationVo;
import com.nofirst.spring.tdd.zhihu.security.CustomUserDetailsService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class NotificationsTest extends BaseContainerTest {

    @Autowired
    private QuestionMapper questionMapper;
    @Autowired
    private NotificationMapper notificationMapper;
    @Autowired
    private SubscriptionMapper subscriptionMapper;

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @BeforeEach
    public void setupTestData() {
        NotificationExample example = new NotificationExample();
        example.createCriteria();
        notificationMapper.deleteByExample(example);
    }

    @Test
    @WithUserDetails(value = "John", userDetailsServiceBeanName = "customUserDetailsService")
    void a_notification_is_prepared_when_a_subscribed_question_receives_a_new_answer_by_other_people() throws Exception {
        // given
        Question question = QuestionFactory.createPublishedQuestion();
        question.setUserId(1);
        questionMapper.insert(question);

        Subscription subscription = SubscriptionFactory.createSubscription(1, question.getId());
        subscriptionMapper.insert(subscription);

        NotificationExample notificationExample = new NotificationExample();
        NotificationExample.Criteria criteria = notificationExample.createCriteria();
        // 用户id为 1 的通知
        criteria.andUserIdEqualTo(1);
        long beforeCount = notificationMapper.countByExample(notificationExample);
        assertThat(beforeCount).isEqualTo(0);
        // when
        AnswerDto answer = AnswerFactory.createAnswerDto();
        this.mockMvc.perform(post("/questions/{id}/answers", question.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(answer))
        );

        // then
        long afterCount = notificationMapper.countByExample(notificationExample);
        assertThat(afterCount).isEqualTo(1);
    }

    @Test
    @WithUserDetails(value = "John", userDetailsServiceBeanName = "customUserDetailsService")
    void a_user_can_fetch_their_unread_notifications() throws Exception {
        // given
        Question question = QuestionFactory.createPublishedQuestion();
        question.setUserId(1);
        questionMapper.insert(question);
        // 1号用户（Jane）订阅了一个问题，如果有人发表了答案，Jane 会收到一条通知
        Subscription subscription = SubscriptionFactory.createSubscription(1, question.getId());
        subscriptionMapper.insert(subscription);
        AnswerDto answer = AnswerFactory.createAnswerDto();
        this.mockMvc.perform(post("/questions/{id}/answers", question.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(answer))
        );


        // when：模拟 Jane 用户登录之后的行为
        String jsonResponse = this.mockMvc.perform(get("/notifications?pageIndex=1&pageSize=10")
                        .with(user(customUserDetailsService.loadUserByUsername("Jane")))
                ).andExpect(status().isOk()).andReturn().getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        // then
        TypeReference<CommonResult<PageInfo<NotificationVo>>> typeRef = new TypeReference<>() {
        };
        CommonResult<PageInfo<NotificationVo>> commonResult = objectMapper.readValue(jsonResponse, typeRef);
        long code = commonResult.getCode();
        Assertions.assertThat(code).isEqualTo(ResultCode.SUCCESS.getCode());

        PageInfo<NotificationVo> data = commonResult.getData();
        assertThat(data.getTotal()).isEqualTo(1);
        assertThat(data.getList().size()).isEqualTo(1);
    }

    @Test
    void guests_can_not_see_unread_notifications() throws Exception {
        this.mockMvc.perform(get("/notifications"))
                .andDo(print())
                .andExpect(status().is(401));
    }

    @Test
    @WithUserDetails(value = "John", userDetailsServiceBeanName = "customUserDetailsService")
    void clear_all_unread_notifications_after_see_unread_notifications_page() throws Exception {
        // given
        Question question = QuestionFactory.createPublishedQuestion();
        question.setUserId(1);
        questionMapper.insert(question);
        // 1号和3号用户订阅了一个问题，如果有人发表了答案，他们都会收到一条通知
        Subscription subscriptionOfJane = SubscriptionFactory.createSubscription(1, question.getId());
        subscriptionMapper.insert(subscriptionOfJane);
        Subscription subscriptionOfFoo = SubscriptionFactory.createSubscription(3, question.getId());
        subscriptionMapper.insert(subscriptionOfFoo);

        // 初始的未读通知数量是0
        NotificationExample exampleOfJane = new NotificationExample();
        exampleOfJane.createCriteria().andUserIdEqualTo(1).andReadAtIsNull();
        long beforeCountOfJane = notificationMapper.countByExample(exampleOfJane);
        assertThat(beforeCountOfJane).isEqualTo(0);

        NotificationExample exampleOfFoo = new NotificationExample();
        exampleOfFoo.createCriteria().andUserIdEqualTo(3).andReadAtIsNull();
        long beforeCountOfFoo = notificationMapper.countByExample(exampleOfFoo);
        assertThat(beforeCountOfFoo).isEqualTo(0);

        AnswerDto answer = AnswerFactory.createAnswerDto();
        this.mockMvc.perform(post("/questions/{id}/answers", question.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(answer))
        );
        // 未读通知数量变成1
        long afterCountOfJane = notificationMapper.countByExample(exampleOfJane);
        assertThat(afterCountOfJane).isEqualTo(1);
        long afterCountOfFoo = notificationMapper.countByExample(exampleOfFoo);
        assertThat(afterCountOfFoo).isEqualTo(1);

        // when
        // 切换到1号用户进行访问
        this.mockMvc.perform(get("/notifications?pageIndex=1&pageSize=10")
                .with(user(customUserDetailsService.loadUserByUsername("Jane")))
        ).andExpect(status().isOk()).andReturn();

        // then
        // 最终1号用户未读通知数量变成0
        long finalCountOfJane = notificationMapper.countByExample(exampleOfJane);
        assertThat(finalCountOfJane).isEqualTo(0);
        // 最终3号用户未读通知数量还是1
        long finalCountOfFoo = notificationMapper.countByExample(exampleOfFoo);
        assertThat(finalCountOfFoo).isEqualTo(1);
    }
}