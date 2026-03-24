package com.nofirst.spring.tdd.zhihu.policy;

import com.nofirst.spring.tdd.zhihu.exception.AnswerNotExistedException;
import com.nofirst.spring.tdd.zhihu.mbg.mapper.AnswerMapper;
import com.nofirst.spring.tdd.zhihu.mbg.model.Answer;
import com.nofirst.spring.tdd.zhihu.security.AccountUser;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@AllArgsConstructor
public class AnswerPolicy {

    private final AnswerMapper answerMapper;

    public boolean canDelete(Integer answerId, AccountUser accountUser) {
        Answer answer = answerMapper.selectByPrimaryKey(answerId);
        if (Objects.isNull(answer)) {
            throw new AnswerNotExistedException();
        }
        return accountUser.getUserId().equals(answer.getUserId());
    }
}
