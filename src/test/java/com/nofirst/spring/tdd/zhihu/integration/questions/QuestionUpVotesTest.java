package com.nofirst.spring.tdd.zhihu.integration.questions;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.PageInfo;
import com.nofirst.spring.tdd.zhihu.common.CommonResult;
import com.nofirst.spring.tdd.zhihu.factory.QuestionFactory;
import com.nofirst.spring.tdd.zhihu.integration.AbstractVoteUpTest;
import com.nofirst.spring.tdd.zhihu.mbg.mapper.QuestionMapper;
import com.nofirst.spring.tdd.zhihu.mbg.mapper.VoteMapper;
import com.nofirst.spring.tdd.zhihu.mbg.model.Question;
import com.nofirst.spring.tdd.zhihu.mbg.model.QuestionExample;
import com.nofirst.spring.tdd.zhihu.mbg.model.VoteExample;
import com.nofirst.spring.tdd.zhihu.model.enums.VoteActionType;
import com.nofirst.spring.tdd.zhihu.model.vo.QuestionVo;
import com.nofirst.spring.tdd.zhihu.security.CustomUserDetailsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithUserDetails;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class QuestionUpVotesTest extends AbstractVoteUpTest {

    @Autowired
    private VoteMapper voteMapper;
    @Autowired
    private QuestionMapper questionMapper;

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Override
    protected String getResourceTypeName() {
        return Question.class.getSimpleName();
    }

    @Override
    protected String getResourcePath() {
        return "questions";
    }

    @BeforeEach
    public void setupTestData() {
        VoteExample voteExample = new VoteExample();
        voteExample.createCriteria();
        voteMapper.deleteByExample(voteExample);
        QuestionExample questionExample = new QuestionExample();
        questionExample.createCriteria();
        questionMapper.deleteByExample(questionExample);
    }

    @Test
    @WithUserDetails(value = "John", userDetailsServiceBeanName = "customUserDetailsService")
    void question_can_know_it_is_voted_up() throws Exception {
        // given
        Question publishedQuestion = QuestionFactory.createPublishedQuestion();
        questionMapper.insert(publishedQuestion);
        // vote up
        this.mockMvc.perform(post(getUpVoteUrl(publishedQuestion.getId())));

        // when
        String json = this.mockMvc.perform(get("/questions?pageIndex=1&pageSize=20", publishedQuestion.getId()))
                .andExpect(status().isOk()).andReturn().getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        // then
        TypeReference<CommonResult<PageInfo<QuestionVo>>> typeRef = new TypeReference<>() {
        };
        CommonResult<PageInfo<QuestionVo>> pageResult = objectMapper.readValue(json, typeRef);
        List<QuestionVo> data = pageResult.getData().getList();
        assertThat(data.size()).isEqualTo(1);

        assertThat(data.get(0).getId()).isEqualTo(publishedQuestion.getId());
        assertThat(data.get(0).getVoteType()).isEqualTo(VoteActionType.VOTE_UP.getCode());

        // 切换到1号用户进行访问
        json = this.mockMvc.perform(get("/questions?pageIndex=1&pageSize=20", publishedQuestion.getId())
                        .with(user(customUserDetailsService.loadUserByUsername("Jane")))
                ).andExpect(status().isOk()).andReturn().getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
        pageResult = objectMapper.readValue(json, typeRef);
        data = pageResult.getData().getList();
        assertThat(data.size()).isEqualTo(1);
        // 对1号用户而言，是没有推荐过的
        assertThat(data.get(0).getId()).isEqualTo(publishedQuestion.getId());
        assertThat(data.get(0).getVoteType()).isEqualTo(VoteActionType.NOTHING.getCode());
    }

    @Test
    @WithUserDetails(value = "John", userDetailsServiceBeanName = "customUserDetailsService")
    void can_know_up_votes_count() throws Exception {
        // given
        Question publishedQuestion = QuestionFactory.createPublishedQuestion();
        questionMapper.insert(publishedQuestion);
        // 2号用户 vote up
        this.mockMvc.perform(post(getUpVoteUrl(publishedQuestion.getId()))).andDo(print());
        // 1号用户 vote up
        this.mockMvc.perform(post(getUpVoteUrl(publishedQuestion.getId()))
                .with(user(customUserDetailsService.loadUserByUsername("Jane")))).andDo(print());

        // when
        String json = this.mockMvc.perform(get("/questions?pageIndex=1&pageSize=20", publishedQuestion.getId()))
                .andExpect(status().isOk()).andReturn().getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        // then
        TypeReference<CommonResult<PageInfo<QuestionVo>>> typeRef = new TypeReference<>() {
        };
        CommonResult<PageInfo<QuestionVo>> pageResult = objectMapper.readValue(json, typeRef);
        List<QuestionVo> data = pageResult.getData().getList();
        assertThat(data.size()).isEqualTo(1);
        assertThat(data.get(0).getVoteUpCount()).isEqualTo(2);
    }
}