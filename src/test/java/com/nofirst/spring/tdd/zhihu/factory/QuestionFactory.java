package com.nofirst.spring.tdd.zhihu.factory;

import com.nofirst.spring.tdd.zhihu.mbg.model.Question;
import com.nofirst.spring.tdd.zhihu.model.dto.QuestionDto;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class QuestionFactory {

    public static Question createPublishedQuestion() {
        Date now = new Date();

        Question question = new Question();
        question.setUserId(1);
        question.setTitle("this is a question");
        question.setContent("this is content");
        question.setCreatedAt(now);
        question.setUpdatedAt(now);
        question.setPublishedAt(now);
        question.setCategoryId(1);
        question.setAnswersCount(0);

        return question;
    }

    public static Question createUnpublishedQuestion() {
        Date now = new Date();

        Question question = new Question();
        question.setUserId(1);
        question.setTitle("this is a question");
        question.setContent("this is content");
        question.setCreatedAt(now);
        question.setUpdatedAt(now);
        question.setPublishedAt(null);
        question.setCategoryId(1);
        question.setAnswersCount(0);

        return question;
    }

    public static QuestionDto createQuestionDto() {
        QuestionDto questionDto = new QuestionDto();
        questionDto.setTitle("this is a new question");
        questionDto.setContent("question content");
        questionDto.setCategoryId(1);

        return questionDto;
    }

    public static List<Question> createPublishedQuestionBatch(Integer times) {
        List<Question> questions = new ArrayList<>();
        for (int i = 0; i < times; i++) {
            questions.add(createPublishedQuestion());
        }

        return questions;
    }
}