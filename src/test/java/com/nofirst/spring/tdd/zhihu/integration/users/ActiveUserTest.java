package com.nofirst.spring.tdd.zhihu.integration.users;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nofirst.spring.tdd.zhihu.common.CommonResult;
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
import com.nofirst.spring.tdd.zhihu.model.vo.UserVo;
import com.nofirst.spring.tdd.zhihu.redis.JsonRedisTemplate;
import com.nofirst.spring.tdd.zhihu.task.ActiveUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithUserDetails;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ActiveUserTest extends BaseContainerTest {

    @Autowired
    private ActiveUserService activeUserService;

    @Autowired
    private JsonRedisTemplate jsonRedisTemplate;

    @Autowired
    private QuestionMapper questionMapper;

    @Autowired
    private AnswerMapper answerMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setupTestData() {
        QuestionExample questionExample = new QuestionExample();
        questionExample.createCriteria();
        questionMapper.deleteByExample(questionExample);
        AnswerExample answerExample = new AnswerExample();
        answerExample.createCriteria();
        answerMapper.deleteByExample(answerExample);
    }

    @Test
    void can_calculate_active_user() {
        jsonRedisTemplate.delete(ActiveUserService.CACHE_KEY);
        // given
        // John 创建了 1 个 Question，得 4 分
        Question question = QuestionFactory.createPublishedQuestion();
        question.setUserId(2);
        questionMapper.insert(question);
        // Jane 创建了 1 个 Answer，得 1 分
        Answer answer = AnswerFactory.createAnswer(question.getId());
        answer.setUserId(1);
        answerMapper.insert(answer);
        // 还有一个用户 Foo，不得分

        // when
        activeUserService.calculateAndCacheActiveUsers();

        // then
        List<UserVo> userVos = (List<UserVo>) jsonRedisTemplate.opsForValue().get(ActiveUserService.CACHE_KEY);
        assertThat(userVos).isNotNull();
        assertThat(userVos.size()).isEqualTo(2);
        assertThat(userVos.stream().map(UserVo::getId).collect(Collectors.toList())).isEqualTo(Arrays.asList(2, 1));
    }

    @Test
    @WithUserDetails(value = "John", userDetailsServiceBeanName = "customUserDetailsService")
    void can_get_all_active_user() throws Exception {
        // 先清除数据，避免脏数据干扰
        jsonRedisTemplate.delete(ActiveUserService.CACHE_KEY);
        // given
        // John 创建了 1 个 Question，得 4 分
        Question question = QuestionFactory.createPublishedQuestion();
        question.setUserId(2);
        questionMapper.insert(question);
        // Jane 创建了 1 个 Answer，得 1 分
        Answer answer = AnswerFactory.createAnswer(question.getId());
        answer.setUserId(1);
        answerMapper.insert(answer);
        // 还有一个用户 Foo，不得分

        // when
        String jsonResponse = this.mockMvc.perform(get("/active-users"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        // then
        TypeReference<CommonResult<List<UserVo>>> typeRef = new TypeReference<>() {
        };
        CommonResult<List<UserVo>> commonResult = objectMapper.readValue(jsonResponse, typeRef);
        long code = commonResult.getCode();
        assertThat(code).isEqualTo(ResultCode.SUCCESS.getCode());
        assertThat(commonResult.getData().size()).isEqualTo(2);
        assertThat(commonResult.getData().stream().map(UserVo::getId).collect(Collectors.toList()))
                .isEqualTo(Arrays.asList(2, 1));
    }
}
