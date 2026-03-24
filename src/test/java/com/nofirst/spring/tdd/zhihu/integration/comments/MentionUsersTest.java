package com.nofirst.spring.tdd.zhihu.integration.comments;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nofirst.spring.tdd.zhihu.common.ResultCode;
import com.nofirst.spring.tdd.zhihu.factory.AnswerFactory;
import com.nofirst.spring.tdd.zhihu.factory.QuestionFactory;
import com.nofirst.spring.tdd.zhihu.integration.BaseContainerTest;
import com.nofirst.spring.tdd.zhihu.mbg.mapper.AnswerMapper;
import com.nofirst.spring.tdd.zhihu.mbg.mapper.NotificationMapper;
import com.nofirst.spring.tdd.zhihu.mbg.mapper.QuestionMapper;
import com.nofirst.spring.tdd.zhihu.mbg.model.Answer;
import com.nofirst.spring.tdd.zhihu.mbg.model.NotificationExample;
import com.nofirst.spring.tdd.zhihu.mbg.model.Question;
import com.nofirst.spring.tdd.zhihu.model.dto.CommentDto;
import org.assertj.core.api.AssertionsForClassTypes;
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

class MentionUsersTest extends BaseContainerTest {

    @Autowired
    private QuestionMapper questionMapper;

    @Autowired
    private AnswerMapper answerMapper;

    @Autowired
    private NotificationMapper notificationMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setupTestData() {
        // 清空通知表
        NotificationExample notificationExample = new NotificationExample();
        notificationExample.createCriteria();
        notificationMapper.deleteByExample(notificationExample);
    }

    @Test
    @WithUserDetails(value = "John", userDetailsServiceBeanName = "customUserDetailsService")
    void mentioned_users_are_notified_when_comment_a_question() throws Exception {
        // given
        Question question = QuestionFactory.createPublishedQuestion();
        questionMapper.insert(question);

        NotificationExample notificationExample = new NotificationExample();
        NotificationExample.Criteria criteria = notificationExample.createCriteria();
        // 查用户id为1的，也就是 Jane
        criteria.andUserIdEqualTo(1);
        long beforeCountOfJane = notificationMapper.countByExample(notificationExample);
        AssertionsForClassTypes.assertThat(beforeCountOfJane).isEqualTo(0);
        // 查用户id为3的，也就是 Foo
        notificationExample.clear();
        criteria.andUserIdEqualTo(3);
        long beforeCountOfFoo = notificationMapper.countByExample(notificationExample);
        AssertionsForClassTypes.assertThat(beforeCountOfFoo).isEqualTo(0);
        // when
        CommentDto commentDto = new CommentDto();
        commentDto.setContent("@Jane @Foo look at this");
        this.mockMvc.perform(post("/comments/questions/{questionId}", question.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentDto))
                ).andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ResultCode.SUCCESS.getCode()));

        // then
        // 查用户id为1的，也就是 Jane
        notificationExample.clear();
        criteria = notificationExample.createCriteria();
        criteria.andUserIdEqualTo(1);
        long afterCountOfJane = notificationMapper.countByExample(notificationExample);
        AssertionsForClassTypes.assertThat(afterCountOfJane).isEqualTo(1);
        // 查用户id为3的，也就是 Foo
        notificationExample.clear();
        criteria = notificationExample.createCriteria();
        criteria.andUserIdEqualTo(3);
        long afterCountOfFoo = notificationMapper.countByExample(notificationExample);
        assertThat(afterCountOfFoo).isEqualTo(1);
    }

    @Test
    @WithUserDetails(value = "John", userDetailsServiceBeanName = "customUserDetailsService")
    void mentioned_users_are_notified_when_comment_a_answer() throws Exception {
        // given
        Answer answer = AnswerFactory.createAnswer(1);
        answerMapper.insert(answer);

        NotificationExample notificationExample = new NotificationExample();
        NotificationExample.Criteria criteria = notificationExample.createCriteria();
        // 查用户 id 为 1 的，也就是 Jane
        criteria.andUserIdEqualTo(1);
        long beforeCountOfJane = notificationMapper.countByExample(notificationExample);
        AssertionsForClassTypes.assertThat(beforeCountOfJane).isEqualTo(0);
        // 查用户 id 为 3 的，也就是 Foo
        notificationExample.clear();
        criteria.andUserIdEqualTo(3);
        long beforeCountOfFoo = notificationMapper.countByExample(notificationExample);
        AssertionsForClassTypes.assertThat(beforeCountOfFoo).isEqualTo(0);

        // when
        CommentDto commentDto = new CommentDto();
        commentDto.setContent("@Jane @Foo look at this answer");
        this.mockMvc.perform(post("/comments/answers/{answerId}", answer.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentDto))
                ).andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ResultCode.SUCCESS.getCode()));

        // then
        // 查用户 id 为 1 的，也就是 Jane
        notificationExample.clear();
        criteria = notificationExample.createCriteria();
        criteria.andUserIdEqualTo(1);
        long afterCountOfJane = notificationMapper.countByExample(notificationExample);
        AssertionsForClassTypes.assertThat(afterCountOfJane).isEqualTo(1);
        // 查用户 id 为 3 的，也就是 Foo
        notificationExample.clear();
        criteria = notificationExample.createCriteria();
        criteria.andUserIdEqualTo(3);
        long afterCountOfFoo = notificationMapper.countByExample(notificationExample);
        AssertionsForClassTypes.assertThat(afterCountOfFoo).isEqualTo(1);
    }
}