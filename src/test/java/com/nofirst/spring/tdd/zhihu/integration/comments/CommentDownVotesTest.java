package com.nofirst.spring.tdd.zhihu.integration.comments;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.PageInfo;
import com.nofirst.spring.tdd.zhihu.common.CommonResult;
import com.nofirst.spring.tdd.zhihu.factory.CommentFactory;
import com.nofirst.spring.tdd.zhihu.factory.QuestionFactory;
import com.nofirst.spring.tdd.zhihu.integration.AbstractVoteDownTest;
import com.nofirst.spring.tdd.zhihu.mbg.mapper.CommentMapper;
import com.nofirst.spring.tdd.zhihu.mbg.mapper.QuestionMapper;
import com.nofirst.spring.tdd.zhihu.mbg.mapper.VoteMapper;
import com.nofirst.spring.tdd.zhihu.mbg.model.Comment;
import com.nofirst.spring.tdd.zhihu.mbg.model.CommentExample;
import com.nofirst.spring.tdd.zhihu.mbg.model.Question;
import com.nofirst.spring.tdd.zhihu.mbg.model.VoteExample;
import com.nofirst.spring.tdd.zhihu.model.enums.VoteActionType;
import com.nofirst.spring.tdd.zhihu.model.vo.CommentVo;
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

class CommentDownVotesTest extends AbstractVoteDownTest {

    @Autowired
    private VoteMapper voteMapper;
    @Autowired
    private CommentMapper commentMapper;
    @Autowired
    private QuestionMapper questionMapper;

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Override
    protected String getResourceTypeName() {
        return Comment.class.getSimpleName();
    }

    @Override
    protected String getResourcePath() {
        return "comments";
    }

    @BeforeEach
    public void setupTestData() {
        VoteExample voteExample = new VoteExample();
        voteExample.createCriteria();
        voteMapper.deleteByExample(voteExample);
        CommentExample commentExample = new CommentExample();
        commentExample.createCriteria();
        commentMapper.deleteByExample(commentExample);
    }

    @Test
    @WithUserDetails(value = "John", userDetailsServiceBeanName = "customUserDetailsService")
    void comment_can_know_it_is_voted_down() throws Exception {
        // given
        Question question = QuestionFactory.createPublishedQuestion();
        questionMapper.insert(question);
        Comment commentWithoutVoting = CommentFactory.create(question.getId(), question.getClass().getSimpleName());
        commentMapper.insert(commentWithoutVoting);
        Comment commentWithVoting = CommentFactory.create(question.getId(), question.getClass().getSimpleName());
        commentMapper.insert(commentWithVoting);
        // vote up
        this.mockMvc.perform(post(getDownVoteUrl(commentWithVoting.getId())));

        // when
        String json = this.mockMvc.perform(get("/comments/questions/{questionId}?pageIndex=1&pageSize=20", question.getId()))
                .andExpect(status().isOk()).andReturn().getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        // then
        TypeReference<CommonResult<PageInfo<CommentVo>>> typeRef = new TypeReference<>() {
        };
        CommonResult<PageInfo<CommentVo>> pageResult = objectMapper.readValue(json, typeRef);
        List<CommentVo> data = pageResult.getData().getList();
        assertThat(data.size()).isEqualTo(2);

        assertThat(data.get(0).getId()).isEqualTo(commentWithoutVoting.getId());
        assertThat(data.get(0).getVoteType()).isEqualTo(VoteActionType.NOTHING.getCode());

        assertThat(data.get(1).getId()).isEqualTo(commentWithVoting.getId());
        assertThat(data.get(1).getVoteType()).isEqualTo(VoteActionType.VOTE_DOWN.getCode());

        // 切换到1号用户进行访问
        json = this.mockMvc.perform(get("/comments/questions/{questionId}?pageIndex=1&pageSize=20", question.getId())
                        .with(user(customUserDetailsService.loadUserByUsername("Jane")))
                ).andExpect(status().isOk()).andReturn().getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
        pageResult = objectMapper.readValue(json, typeRef);
        data = pageResult.getData().getList();
        assertThat(data.size()).isEqualTo(2);
        // 对1号用户而言，两条都是没有点过赞的
        assertThat(data.get(0).getId()).isEqualTo(commentWithoutVoting.getId());
        assertThat(data.get(0).getVoteType()).isEqualTo(VoteActionType.NOTHING.getCode());
        assertThat(data.get(1).getId()).isEqualTo(commentWithVoting.getId());
        assertThat(data.get(1).getVoteType()).isEqualTo(VoteActionType.NOTHING.getCode());
    }

    @Test
    @WithUserDetails(value = "John", userDetailsServiceBeanName = "customUserDetailsService")
    void can_know_up_votes_count() throws Exception {
        // given
        Question question = QuestionFactory.createPublishedQuestion();
        questionMapper.insert(question);
        Comment comment = CommentFactory.create(question.getId(), question.getClass().getSimpleName());
        commentMapper.insert(comment);
        // 2号用户 vote down
        this.mockMvc.perform(post(getDownVoteUrl(comment.getId()))).andDo(print());
        // 1号用户 vote down
        this.mockMvc.perform(post(getDownVoteUrl(comment.getId()))
                .with(user(customUserDetailsService.loadUserByUsername("Jane")))).andDo(print());

        // when
        String json = this.mockMvc.perform(get("/comments/questions/{questionId}?pageIndex=1&pageSize=20", question.getId()))
                .andExpect(status().isOk()).andReturn().getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        // then
        TypeReference<CommonResult<PageInfo<CommentVo>>> typeRef = new TypeReference<>() {
        };
        CommonResult<PageInfo<CommentVo>> pageResult = objectMapper.readValue(json, typeRef);
        List<CommentVo> data = pageResult.getData().getList();
        assertThat(data.size()).isEqualTo(1);
        assertThat(data.get(0).getId()).isEqualTo(comment.getId());
        assertThat(data.get(0).getVoteDownCount()).isEqualTo(2);
    }
}