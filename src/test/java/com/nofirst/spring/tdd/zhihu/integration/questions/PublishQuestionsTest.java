package com.nofirst.spring.tdd.zhihu.integration.questions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nofirst.spring.tdd.zhihu.common.ResultCode;
import com.nofirst.spring.tdd.zhihu.factory.QuestionFactory;
import com.nofirst.spring.tdd.zhihu.integration.BaseContainerTest;
import com.nofirst.spring.tdd.zhihu.mbg.mapper.QuestionMapper;
import com.nofirst.spring.tdd.zhihu.mbg.model.Question;
import com.nofirst.spring.tdd.zhihu.mbg.model.QuestionExample;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PublishQuestionsTest extends BaseContainerTest {

    @Autowired
    private QuestionMapper questionMapper;
    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setupTestData() {
        QuestionExample example = new QuestionExample();
        // 空条件，匹配所有数据，等价于 delete * from question
        example.createCriteria();
        questionMapper.deleteByExample(example);
    }

    @Test
    void guests_may_not_publish_questions() throws Exception {
        this.mockMvc.perform(post("/questions/1/published-questions")
                        .contentType(MediaType.APPLICATION_JSON))

                .andDo(print())
                .andExpect(status().is(401));
    }

    @Test
    @WithUserDetails(value = "John", userDetailsServiceBeanName = "customUserDetailsService")
    void can_publish_question() throws Exception {
        // given
        Question question = QuestionFactory.createUnpublishedQuestion();
        // 2号用户就是 John
        question.setUserId(2);
        questionMapper.insert(question);
        QuestionExample example = new QuestionExample();
        QuestionExample.Criteria criteria = example.createCriteria();
        criteria.andPublishedAtIsNotNull();
        long beforeCount = questionMapper.countByExample(example);
        // when
        this.mockMvc.perform(post("/questions/{questionId}/published-questions", question.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(question)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ResultCode.SUCCESS.getCode()));

        // then
        long afterCount = questionMapper.countByExample(example);
        // 调用之后 question 增加了 1 条
        assertThat(afterCount - beforeCount).isEqualTo(1);
    }

    @Test
    @WithUserDetails(value = "John", userDetailsServiceBeanName = "customUserDetailsService")
    void only_the_question_creator_can_publish_it() throws Exception {
        // given
        Question question = QuestionFactory.createUnpublishedQuestion();
        // 1号用户不是 John
        question.setUserId(1);
        questionMapper.insert(question);

        // when
        this.mockMvc.perform(post("/questions/{questionId}/published-questions", question.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(question)))
                .andDo(print())
                .andExpect(status().is(403));

    }
}