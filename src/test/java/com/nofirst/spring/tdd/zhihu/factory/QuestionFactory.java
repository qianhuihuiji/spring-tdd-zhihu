package com.nofirst.spring.tdd.zhihu.factory;

import com.nofirst.spring.tdd.zhihu.mbg.model.Question;
import com.nofirst.spring.tdd.zhihu.model.dto.QuestionDto;
import net.datafaker.Faker;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class QuestionFactory extends EntityFactory<Question> {

    private static final Faker faker = new Faker();

    private boolean published = true;

    public static QuestionFactory of() {
        return new QuestionFactory();
    }

    public QuestionFactory unpublished() {
        this.published = false;
        return this;
    }

    @Override
    protected Question build() {
        Date now = new Date();

        Question question = new Question();
        question.setUserId(1);
        question.setTitle(faker.lorem().sentence());
        question.setContent(faker.lorem().paragraph());
        question.setCreatedAt(now);
        question.setUpdatedAt(now);
        question.setPublishedAt(published ? now : null);
        question.setCategoryId(1);
        question.setAnswersCount(0);

        return question;
    }

    public static Question createPublishedQuestion() {
        return QuestionFactory.of().make();
    }

    public static Question createUnpublishedQuestion() {
        return QuestionFactory.of().unpublished().make();
    }

    public static QuestionDto createQuestionDto() {
        QuestionDto questionDto = new QuestionDto();
        questionDto.setTitle(faker.lorem().sentence());
        questionDto.setContent(faker.lorem().paragraph());
        questionDto.setCategoryId(1);

        return questionDto;
    }

    public static List<Question> createPublishedQuestionBatch(Integer times) {
        return QuestionFactory.of().make(times);
    }
}
