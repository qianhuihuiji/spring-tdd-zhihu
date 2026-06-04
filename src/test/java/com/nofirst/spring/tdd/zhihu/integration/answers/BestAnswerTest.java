package com.nofirst.spring.tdd.zhihu.integration.answers;

import com.nofirst.spring.tdd.zhihu.common.ResultCode;
import com.nofirst.spring.tdd.zhihu.integration.BaseContainerTest;
import com.nofirst.spring.tdd.zhihu.mbg.mapper.QuestionMapper;
import com.nofirst.spring.tdd.zhihu.mbg.model.Answer;
import com.nofirst.spring.tdd.zhihu.mbg.model.Question;
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

class BestAnswerTest extends BaseContainerTest {

    @Autowired
    private QuestionMapper questionMapper;

    @BeforeEach
    public void setupTestData() {
        seeder().cleanAll();
    }

    @Test
    void guests_can_not_mark_best_answer() throws Exception {
        this.mockMvc.perform(post("/answers/{id}/best", 1))
                .andExpect(status().is(401));
    }

    @Test
    @WithUserDetails(value = "John", userDetailsServiceBeanName = "customUserDetailsService")
    void only_the_question_creator_can_mark_a_best_answer() throws Exception {
        // given
        Question questionOfOtherUser = seeder().aQuestion(1);
        Answer answerOfOther = seeder().anAnswer(questionOfOtherUser.getId(), 1);

        // when
        this.mockMvc.perform(post("/answers/{answerId}/best", answerOfOther.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                ).andDo(print())
                .andExpect(status().is(403));

        // given
        Question questionOfJohn = seeder().aQuestion(2);
        Answer answerOfJohn = seeder().anAnswer(questionOfJohn.getId(), 2);

        // when
        this.mockMvc.perform(post("/answers/{answerId}/best", answerOfJohn.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                ).andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ResultCode.SUCCESS.getCode()));

        // then
        Question questionAfter = questionMapper.selectByPrimaryKey(questionOfJohn.getId());
        assertThat(questionAfter.getBestAnswerId()).isEqualTo(answerOfJohn.getId());
    }
}
