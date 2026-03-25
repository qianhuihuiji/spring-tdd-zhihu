package com.nofirst.spring.tdd.zhihu.integration.answers;

import com.nofirst.spring.tdd.zhihu.common.ResultCode;
import com.nofirst.spring.tdd.zhihu.factory.AnswerFactory;
import com.nofirst.spring.tdd.zhihu.factory.QuestionFactory;
import com.nofirst.spring.tdd.zhihu.integration.BaseContainerTest;
import com.nofirst.spring.tdd.zhihu.mbg.mapper.AnswerMapper;
import com.nofirst.spring.tdd.zhihu.mbg.mapper.QuestionMapper;
import com.nofirst.spring.tdd.zhihu.mbg.model.Answer;
import com.nofirst.spring.tdd.zhihu.mbg.model.AnswerExample;
import com.nofirst.spring.tdd.zhihu.mbg.model.Question;
import com.nofirst.spring.tdd.zhihu.mbg.model.QuestionExample;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class DeleteAnswerTest extends BaseContainerTest {

    @Autowired
    private QuestionMapper questionMapper;

    @Autowired
    private AnswerMapper answerMapper;

    @BeforeEach
    public void setupTestData() {
        cleanUpQuestions();

        cleanUpAnswers();
    }

    @Test
    void guests_cannot_delete_answers() throws Exception {
        // 目前这个路由还不存在，但是不影响 401 的返回
        this.mockMvc.perform(delete("/answers/{id}", 1))
                .andExpect(status().is(401));
    }

    @Test
    @WithUserDetails(value = "John", userDetailsServiceBeanName = "customUserDetailsService")
    void unauthorized_users_cannot_delete_answers() throws Exception {
        // given：准备测试数据
        Question questionOfOtherUser = QuestionFactory.createPublishedQuestion();
        questionOfOtherUser.setUserId(1);
        questionMapper.insert(questionOfOtherUser);
        Answer answerOfOther = AnswerFactory.createAnswer(questionOfOtherUser.getId());
        answerMapper.insert(answerOfOther);

        // when
        this.mockMvc.perform(delete("/answers/{id}", answerOfOther.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                ).andDo(print())
                .andExpect(status().is(403));
    }

    @Test
    @WithUserDetails(value = "John", userDetailsServiceBeanName = "customUserDetailsService")
    void authorized_users_can_delete_answers() throws Exception {
        // given：准备测试数据
        Question questionOfJohn = QuestionFactory.createPublishedQuestion();
        questionOfJohn.setUserId(2);
        questionOfJohn.setAnswersCount(10);
        questionMapper.insert(questionOfJohn);
        Answer answerOfJohn = AnswerFactory.createAnswer(questionOfJohn.getId());
        answerOfJohn.setUserId(2);
        answerMapper.insert(answerOfJohn);

        AnswerExample answerExample = new AnswerExample();
        AnswerExample.Criteria criteria = answerExample.createCriteria();
        // John 用户的userId就是2
        criteria.andUserIdEqualTo(2);
        long beforeCount = answerMapper.countByExample(answerExample);
        assertThat(beforeCount).isEqualTo(1);

        // when
        this.mockMvc.perform(delete("/answers/{id}", answerOfJohn.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                ).andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ResultCode.SUCCESS.getCode()));

        // then
        long afterCount = answerMapper.countByExample(answerExample);
        assertThat(afterCount).isEqualTo(0);

        Question questionAfter = questionMapper.selectByPrimaryKey(questionOfJohn.getId());
        assertThat(questionAfter.getAnswersCount()).isEqualTo(9);
    }
}
