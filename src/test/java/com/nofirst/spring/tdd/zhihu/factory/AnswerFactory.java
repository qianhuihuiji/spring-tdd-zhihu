package com.nofirst.spring.tdd.zhihu.factory;

import com.nofirst.spring.tdd.zhihu.mbg.model.Answer;
import com.nofirst.spring.tdd.zhihu.model.dto.AnswerDto;
import net.datafaker.Faker;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AnswerFactory extends EntityFactory<Answer> {

    private static final Faker faker = new Faker();

    private Integer questionId = 1;
    private Integer userId = 1;

    public static AnswerFactory of() {
        return new AnswerFactory();
    }

    public AnswerFactory forQuestion(Integer questionId) {
        this.questionId = questionId;
        return this;
    }

    public AnswerFactory byUser(Integer userId) {
        this.userId = userId;
        return this;
    }

    @Override
    protected Answer build() {
        Date now = new Date();
        Answer answer = new Answer();
        answer.setId(1);
        answer.setQuestionId(questionId);
        answer.setUserId(userId);
        answer.setCreatedAt(now);
        answer.setUpdatedAt(now);
        answer.setContent(faker.lorem().paragraph());

        return answer;
    }

    public static AnswerDto createAnswerDto() {
        AnswerDto answer = new AnswerDto();
        answer.setContent(faker.lorem().paragraph());

        return answer;
    }

    public static Answer createAnswer(Integer questionId) {
        return AnswerFactory.of().forQuestion(questionId).make();
    }

    public static List<Answer> createAnswerBatch(Integer times, Integer questionId) {
        return AnswerFactory.of().forQuestion(questionId).make(times);
    }
}
