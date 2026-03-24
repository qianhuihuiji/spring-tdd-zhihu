package com.nofirst.spring.tdd.zhihu.matcher;

import com.nofirst.spring.tdd.zhihu.mbg.model.Answer;
import org.mockito.ArgumentMatcher;

public class AnswerMatcher implements ArgumentMatcher<Answer> {

    private final Answer another;

    public AnswerMatcher(Answer another) {
        this.another = another;
    }

    @Override
    public boolean matches(Answer self) {
        return another.getQuestionId().equals(self.getQuestionId()) &&
                another.getContent().equals(self.getContent()) &&
                another.getUserId().equals(self.getUserId());
    }
}