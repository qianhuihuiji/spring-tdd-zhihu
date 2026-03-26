package com.nofirst.spring.tdd.zhihu.integration.questions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nofirst.spring.tdd.zhihu.common.ResultCode;
import com.nofirst.spring.tdd.zhihu.factory.QuestionFactory;
import com.nofirst.spring.tdd.zhihu.integration.BaseContainerTest;
import com.nofirst.spring.tdd.zhihu.mbg.mapper.QuestionMapper;
import com.nofirst.spring.tdd.zhihu.mbg.model.Question;
import com.nofirst.spring.tdd.zhihu.mbg.model.QuestionExample;
import com.nofirst.spring.tdd.zhihu.model.dto.QuestionDto;
import com.nofirst.spring.tdd.zhihu.service.TranslatorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;

import java.time.Duration;
import java.util.List;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

class CreateQuestionsTest extends BaseContainerTest {

    @Autowired
    private QuestionMapper questionMapper;
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TranslatorService translatorService;

    @BeforeEach
    public void setupTestData() {
        cleanUpQuestions();
        when(translatorService.translate(anyString())).thenReturn("english-english");
    }

    @Test
    void guests_may_not_create_questions() throws Exception {
        this.mockMvc.perform(post("/questions")
                        .contentType(MediaType.APPLICATION_JSON))

                .andDo(print())
                .andExpect(status().is(401));
    }

    @Test
    @WithUserDetails(value = "John", userDetailsServiceBeanName = "customUserDetailsService")
    void an_authenticated_user_can_create_new_questions() throws Exception {
        // given
        QuestionDto questionDto = QuestionFactory.createQuestionDto();
        QuestionExample example = new QuestionExample();
        example.createCriteria();
        long beforeCount = questionMapper.countByExample(example);

        // when
        this.mockMvc.perform(post("/questions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(questionDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ResultCode.SUCCESS.getCode()));

        // then
        long afterCount = questionMapper.countByExample(example);
        // 调用之后 questionDto 增加了 1 条
        assertThat(afterCount - beforeCount).isEqualTo(1);
    }

    @Test
    @WithUserDetails(value = "John", userDetailsServiceBeanName = "customUserDetailsService")
    void title_is_required() throws Exception {
        // given
        QuestionDto questionDto = QuestionFactory.createQuestionDto();
        questionDto.setTitle("");

        // when
        this.mockMvc.perform(post("/questions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(questionDto)))
                // then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ResultCode.VALIDATE_FAILED.getCode()))
                .andExpect(jsonPath("$.message").value("标题不能为空"));
    }

    @Test
    @WithUserDetails(value = "John", userDetailsServiceBeanName = "customUserDetailsService")
    void content_is_required() throws Exception {
        // given
        QuestionDto questionDto = QuestionFactory.createQuestionDto();
        questionDto.setContent("");

        // when
        this.mockMvc.perform(post("/questions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(questionDto)))
                // then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ResultCode.VALIDATE_FAILED.getCode()))
                .andExpect(jsonPath("$.message").value("内容不能为空"));
    }

    @Test
    @WithUserDetails(value = "John", userDetailsServiceBeanName = "customUserDetailsService")
    void category_id_is_required() throws Exception {
        // given
        QuestionDto questionDto = QuestionFactory.createQuestionDto();
        questionDto.setCategoryId(null);

        // when
        this.mockMvc.perform(post("/questions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(questionDto)))
                // then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ResultCode.VALIDATE_FAILED.getCode()))
                .andExpect(jsonPath("$.message").value("问题分类不能为空"));
    }

    @Test
    @WithUserDetails(value = "John", userDetailsServiceBeanName = "customUserDetailsService")
    void category_id_is_valid() throws Exception {
        // given
        QuestionDto questionDto = QuestionFactory.createQuestionDto();
        // -1 的分类在正常情况下是不会存在的
        questionDto.setCategoryId(-1);

        // when
        this.mockMvc.perform(post("/questions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(questionDto)))
                // then
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.code").value(ResultCode.VALIDATE_FAILED.getCode()))
                .andExpect(jsonPath("$.message").value("问题分类不存在"));
    }

    @Test
    @WithUserDetails(value = "John", userDetailsServiceBeanName = "customUserDetailsService")
    void get_slug_when_create_a_question() throws Exception {
        // given
        QuestionDto questionDto = QuestionFactory.createQuestionDto();
        questionDto.setTitle("英语 英语");
        QuestionExample questionExample = new QuestionExample();
        questionExample.createCriteria();
        long beforeCount = questionMapper.countByExample(questionExample);

        // when
        this.mockMvc.perform(post("/questions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(questionDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ResultCode.SUCCESS.getCode()));

        await()
                .pollInterval(Duration.ofSeconds(1))
                .atMost(5, SECONDS)
                .untilAsserted(() -> {
                    // then
                    long afterCount = questionMapper.countByExample(questionExample);
                    // 调用之后 question 增加了 1 条
                    assertThat(afterCount - beforeCount).isEqualTo(1);
                    List<Question> questions = questionMapper.selectByExample(questionExample);
                    Question result = questions.get(0);
                    assertThat(result.getSlug()).isEqualTo("english-english");
                });
    }
}
