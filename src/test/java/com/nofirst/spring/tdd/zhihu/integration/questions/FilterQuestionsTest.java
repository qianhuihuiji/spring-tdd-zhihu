package com.nofirst.spring.tdd.zhihu.integration.questions;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.PageInfo;
import com.nofirst.spring.tdd.zhihu.common.CommonResult;
import com.nofirst.spring.tdd.zhihu.common.ResultCode;
import com.nofirst.spring.tdd.zhihu.factory.QuestionFactory;
import com.nofirst.spring.tdd.zhihu.integration.BaseContainerTest;
import com.nofirst.spring.tdd.zhihu.mbg.mapper.CategoryMapper;
import com.nofirst.spring.tdd.zhihu.mbg.mapper.QuestionMapper;
import com.nofirst.spring.tdd.zhihu.mbg.model.Category;
import com.nofirst.spring.tdd.zhihu.mbg.model.Question;
import com.nofirst.spring.tdd.zhihu.mbg.model.QuestionExample;
import com.nofirst.spring.tdd.zhihu.model.vo.QuestionVo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class FilterQuestionsTest extends BaseContainerTest {

    @Autowired
    private QuestionMapper questionMapper;
    @Autowired
    private CategoryMapper categoryMapper;
    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setupTestData() {
        QuestionExample example = new QuestionExample();
        example.createCriteria();
        questionMapper.deleteByExample(example);
    }

    @Test
    void user_can_see_published_questions_without_any_filter() throws Exception {
        // given
        // 10 条已发布
        List<Question> publishedQuestions = QuestionFactory.createPublishedQuestionBatch(10);
        publishedQuestions.forEach(t -> questionMapper.insert(t));
        // 中间插入一条未发布的
        Question unpublishedQuestion = QuestionFactory.createUnpublishedQuestion();
        questionMapper.insert(unpublishedQuestion);
        // 再来 30 条已发布
        List<Question> publishedQuestions2 = QuestionFactory.createPublishedQuestionBatch(30);
        publishedQuestions2.forEach(t -> questionMapper.insert(t));

        // when
        MvcResult result = this.mockMvc.perform(get("/questions?pageIndex=1&pageSize=20"))
                .andExpect(status().isOk()).andReturn();

        // then
        String json = result.getResponse().getContentAsString();

        TypeReference<CommonResult<PageInfo<QuestionVo>>> typeRef = new TypeReference<>() {
        };
        CommonResult<PageInfo<QuestionVo>> commonResult = objectMapper.readValue(json, typeRef);
        long code = commonResult.getCode();
        assertThat(code).isEqualTo(ResultCode.SUCCESS.getCode());

        PageInfo<QuestionVo> data = commonResult.getData();

        assertThat(json).doesNotContain(unpublishedQuestion.getContent());
        assertThat(data.getTotal()).isEqualTo(40);
        assertThat(data.getList().size()).isEqualTo(20);
    }

    @Test
    void user_can_filter_questions_by_category() throws Exception {
        // given
        Category category = categoryMapper.selectByPrimaryKey(1);
        Question questionInSlug = QuestionFactory.createPublishedQuestion();
        questionInSlug.setCategoryId(category.getId());
        questionInSlug.setTitle("i am in slug");
        questionMapper.insert(questionInSlug);
        Question questionNotInSlug = QuestionFactory.createPublishedQuestion();
        questionNotInSlug.setCategoryId(-1);
        questionNotInSlug.setTitle("i am not in slug");
        questionMapper.insert(questionNotInSlug);

        // when
        String json = this.mockMvc.perform(get("/questions?slug={slug}&pageIndex=1&pageSize=20", category.getSlug()))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
        // then
        assertThat(json).contains(questionInSlug.getTitle());
        assertThat(json).doesNotContain(questionNotInSlug.getTitle());
    }

    @Test
    void user_can_filter_questions_by_username() throws Exception {
        // given
        Question byJohn = QuestionFactory.createPublishedQuestion();
        byJohn.setTitle("question by john");
        byJohn.setUserId(2);
        questionMapper.insert(byJohn);
        Question byOther = QuestionFactory.createPublishedQuestion();
        byOther.setTitle("question by other");
        byOther.setUserId(999);
        questionMapper.insert(byOther);
        // when
        String json = this.mockMvc.perform(get("/questions?pageIndex=1&pageSize=20&by=John"))
                .andExpect(status().isOk()).andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        // then
        assertThat(json).contains(byJohn.getTitle());
        assertThat(json).doesNotContain(byOther.getTitle());
    }

    @Test
    void user_can_filter_questions_by_popularity() throws Exception {
        // given
        Question oneAnswerQuestion = QuestionFactory.createPublishedQuestion();
        oneAnswerQuestion.setAnswersCount(1);
        questionMapper.insert(oneAnswerQuestion);
        Question twoAnswerQuestion = QuestionFactory.createPublishedQuestion();
        twoAnswerQuestion.setAnswersCount(2);
        questionMapper.insert(twoAnswerQuestion);
        Question threeAnswerQuestion = QuestionFactory.createPublishedQuestion();
        threeAnswerQuestion.setAnswersCount(3);
        questionMapper.insert(threeAnswerQuestion);
        // when
        String json = this.mockMvc.perform(get("/questions?pageIndex=1&pageSize=20&popularity=1"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        // then
        TypeReference<CommonResult<PageInfo<QuestionVo>>> typeRef = new TypeReference<>() {
        };
        CommonResult<PageInfo<QuestionVo>> commonResult = objectMapper.readValue(json, typeRef);
        long code = commonResult.getCode();
        assertThat(code).isEqualTo(ResultCode.SUCCESS.getCode());

        PageInfo<QuestionVo> data = commonResult.getData();
        List<Integer> answersCountList = data.getList().stream().map(QuestionVo::getAnswersCount).map(t -> (Integer) t).collect(Collectors.toList());

        assertThat(Arrays.asList(3, 2, 1)).isEqualTo(answersCountList);
    }

    @Test
    void a_user_can_filter_unanswered_questions() throws Exception {
        // given
        Question oneAnswerQuestion = QuestionFactory.createPublishedQuestion();
        oneAnswerQuestion.setAnswersCount(1);
        questionMapper.insert(oneAnswerQuestion);
        Question noAnswerQuestion = QuestionFactory.createPublishedQuestion();
        noAnswerQuestion.setAnswersCount(0);
        questionMapper.insert(noAnswerQuestion);
        // when
        String json = this.mockMvc.perform(get("/questions?pageIndex=1&pageSize=20&unanswered=1"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
        TypeReference<CommonResult<PageInfo<QuestionVo>>> typeRef = new TypeReference<>() {
        };
        CommonResult<PageInfo<QuestionVo>> commonResult = objectMapper.readValue(json, typeRef);
        long code = commonResult.getCode();
        assertThat(code).isEqualTo(ResultCode.SUCCESS.getCode());

        PageInfo<QuestionVo> data = commonResult.getData();
        List<QuestionVo> questionVos = data.getList();
        assertThat(questionVos.size()).isEqualTo(1);
        assertThat(questionVos.get(0).getId()).isEqualTo(noAnswerQuestion.getId());
    }
}