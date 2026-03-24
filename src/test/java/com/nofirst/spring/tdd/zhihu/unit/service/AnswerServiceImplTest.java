package com.nofirst.spring.tdd.zhihu.unit.service;

import com.github.pagehelper.PageInfo;
import com.nofirst.spring.tdd.zhihu.exception.QuestionNotExistedException;
import com.nofirst.spring.tdd.zhihu.exception.QuestionNotPublishedException;
import com.nofirst.spring.tdd.zhihu.factory.AnswerFactory;
import com.nofirst.spring.tdd.zhihu.factory.QuestionFactory;
import com.nofirst.spring.tdd.zhihu.matcher.AnswerMatcher;
import com.nofirst.spring.tdd.zhihu.mbg.mapper.AnswerMapper;
import com.nofirst.spring.tdd.zhihu.mbg.mapper.QuestionMapper;
import com.nofirst.spring.tdd.zhihu.mbg.mapper.QuestionMapperExt;
import com.nofirst.spring.tdd.zhihu.mbg.mapper.VoteMapper;
import com.nofirst.spring.tdd.zhihu.mbg.mapper.VoteMapperExt;
import com.nofirst.spring.tdd.zhihu.mbg.model.Answer;
import com.nofirst.spring.tdd.zhihu.mbg.model.Question;
import com.nofirst.spring.tdd.zhihu.model.dto.AnswerDto;
import com.nofirst.spring.tdd.zhihu.model.vo.AnswerVo;
import com.nofirst.spring.tdd.zhihu.publisher.CustomEventPublisher;
import com.nofirst.spring.tdd.zhihu.security.AccountUser;
import com.nofirst.spring.tdd.zhihu.service.GenericVoteService;
import com.nofirst.spring.tdd.zhihu.service.impl.AnswerServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AnswerServiceImplTest {

    @InjectMocks
    private AnswerServiceImpl answerService;

    @Mock
    private AnswerMapper answerMapper;
    @Mock
    private QuestionMapper questionMapper;
    @Mock
    private QuestionMapperExt questionMapperExt;
    @Mock
    private VoteMapper voteMapper;
    @Mock
    private VoteMapperExt voteMapperExt;
    @Mock
    private GenericVoteService genericVoteService;
    @Mock
    private CustomEventPublisher customEventPublisher;

    private Answer defaultAnswer;
    private AnswerDto defaultAnswerDto;
    private List<Answer> answers;

    @BeforeEach
    public void setup() {
        this.defaultAnswer = AnswerFactory.createAnswer(1);
        this.defaultAnswerDto = AnswerFactory.createAnswerDto();
        this.answers = AnswerFactory.createAnswerBatch(10, 1);
    }

    @Test
    void can_post_an_answer_to_a_question() {
        // given
        Question question = QuestionFactory.createPublishedQuestion();
        question.setId(1);
        given(questionMapper.selectByPrimaryKey(question.getId())).willReturn(question);
        // when
        AccountUser accountUser = new AccountUser(1, "password", "username");
        answerService.store(1, this.defaultAnswerDto, accountUser);

        // then
        verify(answerMapper, times(1)).insert(argThat(new AnswerMatcher(defaultAnswer)));
    }

    @Test
    void can_not_post_an_answer_to_a_not_existed_question() {
        // given
        given(questionMapper.selectByPrimaryKey(1)).willReturn(null);

        // then
        assertThatThrownBy(() -> {
            // when
            AccountUser accountUser = new AccountUser(1, "password", "username");
            answerService.store(1, this.defaultAnswerDto, accountUser);
        }).isInstanceOf(QuestionNotExistedException.class)
                .hasMessageStartingWith("question not exist");
    }

    @Test
    void can_not_post_an_answer_to_a_not_published_question() {
        // given
        Question question = QuestionFactory.createUnpublishedQuestion();
        question.setId(1);
        given(questionMapper.selectByPrimaryKey(question.getId())).willReturn(question);

        // then
        assertThatThrownBy(() -> {
            // when
            AccountUser accountUser = new AccountUser(1, "password", "username");
            answerService.store(1, this.defaultAnswerDto, accountUser);
        }).isInstanceOf(QuestionNotPublishedException.class)
                .hasMessageStartingWith("question not publish");
    }

    @Test
    void can_mark_one_answer_as_the_best() {
        // given
        Question publishedQuestion = QuestionFactory.createPublishedQuestion();
        Answer answer = AnswerFactory.createAnswer(publishedQuestion.getId());
        publishedQuestion.setBestAnswerId(answer.getId());
        given(answerMapper.selectByPrimaryKey(answer.getId())).willReturn(answer);

        // when
        answerService.markAsBest(1);

        // then
        verify(questionMapperExt, times(1)).markAsBestAnswer(publishedQuestion.getId(), answer.getId());
    }

    @Test
    void can_delete_answer() {
        // given
        Question publishedQuestion = QuestionFactory.createPublishedQuestion();
        publishedQuestion.setId(1);
        Answer answer = AnswerFactory.createAnswer(publishedQuestion.getId());
        publishedQuestion.setBestAnswerId(answer.getId());
        given(answerMapper.selectByPrimaryKey(answer.getId())).willReturn(answer);
        given(questionMapper.selectByPrimaryKey(publishedQuestion.getId())).willReturn(publishedQuestion);
        // when
        answerService.destroy(1);

        // then
        verify(questionMapper, times(1)).updateByPrimaryKeySelective(any());
        verify(answerMapper, times(1)).deleteByPrimaryKey(1);
    }

    @Test
    void a_question_has_many_answers() {
        // given
        given(answerMapper.selectByExample(any())).willReturn(this.answers);

        // when
        AccountUser accountUser = new AccountUser(1, "password", "user");
        PageInfo<AnswerVo> answersPage = answerService.answers(1, 1, 20, accountUser);

        // then
        assertThat(answersPage.getTotal()).isEqualTo(10);
        assertThat(answersPage.getSize()).isEqualTo(10);
    }
}