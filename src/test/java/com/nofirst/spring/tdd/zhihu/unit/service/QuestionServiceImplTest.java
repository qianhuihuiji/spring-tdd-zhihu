package com.nofirst.spring.tdd.zhihu.unit.service;

import com.nofirst.spring.tdd.zhihu.exception.QuestionNotExistedException;
import com.nofirst.spring.tdd.zhihu.exception.QuestionNotPublishedException;
import com.nofirst.spring.tdd.zhihu.factory.QuestionFactory;
import com.nofirst.spring.tdd.zhihu.mbg.mapper.QuestionMapper;
import com.nofirst.spring.tdd.zhihu.mbg.model.Question;
import com.nofirst.spring.tdd.zhihu.model.vo.QuestionVo;
import com.nofirst.spring.tdd.zhihu.security.AccountUser;
import com.nofirst.spring.tdd.zhihu.service.impl.AnswerServiceImpl;
import com.nofirst.spring.tdd.zhihu.service.impl.QuestionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class QuestionServiceImplTest {

    @InjectMocks
    private QuestionServiceImpl questionService;

    @Mock
    private AnswerServiceImpl answerService;

    @Mock
    private QuestionMapper questionMapper;

    private Question question;

    @BeforeEach
    public void setup() {
        question = QuestionFactory.createPublishedQuestion();
    }

    @Test
    void get_existed_question_by_show_method() {
        // given
        given(questionMapper.selectByPrimaryKey(1)).willReturn(this.question);

        // when
        AccountUser accountUser = new AccountUser(1, "password", "user");
        QuestionVo existedQuestion = questionService.show(1, accountUser);

        // then
        assertThat(existedQuestion).isNotNull();
        assertThat(existedQuestion.getId()).isEqualTo(this.question.getId());
        assertThat(existedQuestion.getUserId()).isEqualTo(this.question.getUserId());
        assertThat(existedQuestion.getTitle()).isEqualTo(this.question.getTitle());
        assertThat(existedQuestion.getContent()).isEqualTo(this.question.getContent());
    }

    @Test
    void get_not_existed_question_by_show_method() {
        // given
        given(questionMapper.selectByPrimaryKey(1)).willReturn(null);

        // then
        assertThatThrownBy(() -> {
            // when
            AccountUser accountUser = new AccountUser(1, "password", "user");
            questionService.show(1, accountUser);
        }).isInstanceOf(QuestionNotExistedException.class)
                .hasMessageStartingWith("question not exist");
    }

    @Test
    void get_not_published_question_by_show_method() {
        // given
        this.question.setPublishedAt(null);
        given(questionMapper.selectByPrimaryKey(1)).willReturn(this.question);

        // then
        assertThatThrownBy(() -> {
            // when
            AccountUser accountUser = new AccountUser(1, "password", "user");
            questionService.show(1, accountUser);
        }).isInstanceOf(QuestionNotPublishedException.class)
                .hasMessageStartingWith("question not publish");
    }
}