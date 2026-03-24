package com.nofirst.spring.tdd.zhihu.integration.questions;

import com.nofirst.spring.tdd.zhihu.common.ResultCode;
import com.nofirst.spring.tdd.zhihu.factory.QuestionFactory;
import com.nofirst.spring.tdd.zhihu.integration.BaseContainerTest;
import com.nofirst.spring.tdd.zhihu.mbg.mapper.QuestionMapper;
import com.nofirst.spring.tdd.zhihu.mbg.mapper.SubscriptionMapper;
import com.nofirst.spring.tdd.zhihu.mbg.model.Question;
import com.nofirst.spring.tdd.zhihu.mbg.model.QuestionExample;
import com.nofirst.spring.tdd.zhihu.mbg.model.SubscriptionExample;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithUserDetails;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SubscribeQuestionsTest extends BaseContainerTest {

    @Autowired
    private SubscriptionMapper subscriptionMapper;
    @Autowired
    private QuestionMapper questionMapper;

    @BeforeEach
    public void setupTestData() {
        SubscriptionExample subscriptionExample = new SubscriptionExample();
        subscriptionExample.createCriteria();
        subscriptionMapper.deleteByExample(subscriptionExample);
        QuestionExample questionExample = new QuestionExample();
        questionExample.createCriteria();
        questionMapper.deleteByExample(questionExample);
    }

    @Test
    void guests_may_not_subscribe_to_or_unsubscribe_from_questions() throws Exception {
        this.mockMvc.perform(post("/questions/subscriptions"))
                .andDo(print())
                .andExpect(status().is(401));

        this.mockMvc.perform(delete("/questions/subscriptions"))
                .andDo(print())
                .andExpect(status().is(401));
    }

    @Test
    @WithUserDetails(value = "John", userDetailsServiceBeanName = "customUserDetailsService")
    void a_user_can_subscribe_to_questions() throws Exception {
        // given
        Question question = QuestionFactory.createPublishedQuestion();
        questionMapper.insert(question);

        SubscriptionExample example = new SubscriptionExample();
        SubscriptionExample.Criteria criteria = example.createCriteria();
        criteria.andUserIdEqualTo(2);
        criteria.andQuestionIdEqualTo(question.getId());
        long beforeCount = subscriptionMapper.countByExample(example);
        assertThat(beforeCount).isEqualTo(0);
        // when
        this.mockMvc.perform(post("/questions/{questionId}/subscriptions", question.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ResultCode.SUCCESS.getCode()));

        // then
        long afterCount = subscriptionMapper.countByExample(example);
        assertThat(afterCount).isEqualTo(1);
    }

    @Test
    @WithUserDetails(value = "John", userDetailsServiceBeanName = "customUserDetailsService")
    void a_user_can_unsubscribe_from_questions() throws Exception {
        // given
        Question question = QuestionFactory.createPublishedQuestion();
        questionMapper.insert(question);

        // when
        // 下面的逻辑在上一个测试中已经验证过了
        this.mockMvc.perform(post("/questions/{questionId}/subscriptions", question.getId()));
        this.mockMvc.perform(delete("/questions/{questionId}/subscriptions", question.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ResultCode.SUCCESS.getCode()));

        // then
        SubscriptionExample example = new SubscriptionExample();
        SubscriptionExample.Criteria criteria = example.createCriteria();
        criteria.andIdEqualTo(2);
        criteria.andQuestionIdEqualTo(question.getId());
        long count = subscriptionMapper.countByExample(example);
        assertThat(count).isEqualTo(0);
    }
}