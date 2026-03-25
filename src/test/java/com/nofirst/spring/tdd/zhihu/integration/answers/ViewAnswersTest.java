package com.nofirst.spring.tdd.zhihu.integration.answers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.PageInfo;
import com.nofirst.spring.tdd.zhihu.common.CommonResult;
import com.nofirst.spring.tdd.zhihu.common.ResultCode;
import com.nofirst.spring.tdd.zhihu.factory.AnswerFactory;
import com.nofirst.spring.tdd.zhihu.integration.BaseContainerTest;
import com.nofirst.spring.tdd.zhihu.mbg.mapper.AnswerMapper;
import com.nofirst.spring.tdd.zhihu.mbg.model.Answer;
import com.nofirst.spring.tdd.zhihu.mbg.model.AnswerExample;
import com.nofirst.spring.tdd.zhihu.model.vo.AnswerVo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithUserDetails;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ViewAnswersTest extends BaseContainerTest {

    @Autowired
    private AnswerMapper answerMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setupTestData() {
        cleanUpAnswers();
    }

    @Test
    void guest_can_not_view_answers() throws Exception {
        // given
        // when
        this.mockMvc.perform(get("/answers"))
                // then
                .andExpect(status().is(401));
    }

    @Test
    // 下面这行代码，会在 customUserDetailsService 的 loadUserByUsername() 方法中，将 John 查出来，模拟登录
    @WithUserDetails(value = "John", userDetailsServiceBeanName = "customUserDetailsService")
    void signed_in_user_can_view_answers_page_by_page() throws Exception {
        // given
        //  30 个回答，这样可以分成两页，且每页个数不同
        int questionId = 1;
        List<Answer> answers = AnswerFactory.createAnswerBatch(30, questionId);
        answers.forEach(t -> answerMapper.insert(t));

        // when：访问第一页
        String json = this.mockMvc.perform(get("/questions/{questionId}/answers?pageIndex=1&pageSize=20", questionId))
                .andExpect(status().isOk()).andReturn().getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        // then
        TypeReference<CommonResult<PageInfo<AnswerVo>>> typeRef = new TypeReference<>() {
        };
        CommonResult<PageInfo<AnswerVo>> firstPageResult = objectMapper.readValue(json, typeRef);
        long code = firstPageResult.getCode();
        assertThat(code).isEqualTo(ResultCode.SUCCESS.getCode());

        PageInfo<AnswerVo> data = firstPageResult.getData();
        assertThat(data.getTotal()).isEqualTo(30);
        assertThat(data.getList().size()).isEqualTo(20);


        // when：访问第二页
        json = this.mockMvc.perform(get("/questions/{questionId}/answers?pageIndex=2&pageSize=20", questionId))
                .andExpect(status().isOk()).andReturn().getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        // then
        CommonResult<PageInfo<AnswerVo>> secondPageResult = objectMapper.readValue(json, typeRef);
        data = secondPageResult.getData();
        assertThat(data.getTotal()).isEqualTo(30);
        assertThat(data.getList().size()).isEqualTo(10);
    }
}