package com.nofirst.spring.tdd.zhihu.policy;

import com.nofirst.spring.tdd.zhihu.exception.AnswerNotExistedException;
import com.nofirst.spring.tdd.zhihu.exception.QuestionNotExistedException;
import com.nofirst.spring.tdd.zhihu.exception.QuestionNotPublishedException;
import com.nofirst.spring.tdd.zhihu.mbg.mapper.AnswerMapper;
import com.nofirst.spring.tdd.zhihu.mbg.mapper.QuestionMapper;
import com.nofirst.spring.tdd.zhihu.mbg.model.Answer;
import com.nofirst.spring.tdd.zhihu.mbg.model.Question;
import com.nofirst.spring.tdd.zhihu.security.AccountUser;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@AllArgsConstructor
public class QuestionPolicy {

    private final QuestionMapper questionMapper;
    private final AnswerMapper answerMapper;

    public boolean canMarkAnswerAsBest(Integer answerId, AccountUser accountUser) {
        Answer answer = answerMapper.selectByPrimaryKey(answerId);
        if (Objects.isNull(answer)) {
            throw new AnswerNotExistedException();
        }
        Question question = questionMapper.selectByPrimaryKey(answer.getQuestionId());
        if (Objects.isNull(question)) {
            throw new QuestionNotExistedException();
        }
        if (Objects.isNull(question.getPublishedAt())) {
            throw new QuestionNotPublishedException();
        }
        return accountUser.getUserId().equals(question.getUserId());
    }

    public boolean isQuestionOwner(Integer questionId, AccountUser accountUser) {
        Question question = questionMapper.selectByPrimaryKey(questionId);
        if (Objects.isNull(question)) {
            throw new QuestionNotExistedException();
        }

        return accountUser.getUserId().equals(question.getUserId());
    }

    public boolean canComment(Integer questionId) {
        Question question = questionMapper.selectByPrimaryKey(questionId);
        if (Objects.isNull(question)) {
            throw new QuestionNotExistedException();
        }
        if (Objects.isNull(question.getPublishedAt())) {
            throw new QuestionNotPublishedException();
        }
        return true;
    }
}
