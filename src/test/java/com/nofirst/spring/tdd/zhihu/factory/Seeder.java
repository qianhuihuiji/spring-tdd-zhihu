package com.nofirst.spring.tdd.zhihu.factory;

import com.nofirst.spring.tdd.zhihu.mbg.mapper.AnswerMapper;
import com.nofirst.spring.tdd.zhihu.mbg.mapper.CommentMapper;
import com.nofirst.spring.tdd.zhihu.mbg.mapper.QuestionMapper;
import com.nofirst.spring.tdd.zhihu.mbg.mapper.SubscriptionMapper;
import com.nofirst.spring.tdd.zhihu.mbg.mapper.UserMapper;
import com.nofirst.spring.tdd.zhihu.mbg.mapper.VoteMapper;
import com.nofirst.spring.tdd.zhihu.mbg.model.Answer;
import com.nofirst.spring.tdd.zhihu.mbg.model.Comment;
import com.nofirst.spring.tdd.zhihu.mbg.model.Question;
import com.nofirst.spring.tdd.zhihu.mbg.model.Subscription;
import com.nofirst.spring.tdd.zhihu.mbg.model.User;

public class Seeder {

    private final UserMapper userMapper;
    private final QuestionMapper questionMapper;
    private final AnswerMapper answerMapper;
    private final CommentMapper commentMapper;
    private final VoteMapper voteMapper;
    private final SubscriptionMapper subscriptionMapper;

    public Seeder(UserMapper userMapper, QuestionMapper questionMapper, AnswerMapper answerMapper,
                  CommentMapper commentMapper, VoteMapper voteMapper, SubscriptionMapper subscriptionMapper) {
        this.userMapper = userMapper;
        this.questionMapper = questionMapper;
        this.answerMapper = answerMapper;
        this.commentMapper = commentMapper;
        this.voteMapper = voteMapper;
        this.subscriptionMapper = subscriptionMapper;
    }

    public User aUser() {
        User user = UserFactory.of().make();
        userMapper.insertSelective(user);
        return user;
    }

    public User anUnverifiedUser() {
        User user = UserFactory.of().unverified().make();
        userMapper.insertSelective(user);
        return user;
    }

    public Question aQuestion(Integer userId) {
        Question question = QuestionFactory.of().make();
        question.setUserId(userId);
        questionMapper.insertSelective(question);
        return question;
    }

    public Question anUnpublishedQuestion(Integer userId) {
        Question question = QuestionFactory.of().unpublished().make();
        question.setUserId(userId);
        questionMapper.insertSelective(question);
        return question;
    }

    public Answer anAnswer(Integer questionId, Integer userId) {
        Answer answer = AnswerFactory.of().forQuestion(questionId).byUser(userId).make();
        answerMapper.insertSelective(answer);
        return answer;
    }

    public Comment aComment(Integer commentedId, String commentedType, Integer userId) {
        Comment comment = CommentFactory.of().forTarget(commentedId, commentedType).byUser(userId).make();
        commentMapper.insertSelective(comment);
        return comment;
    }

    public Subscription aSubscription(Integer userId, Integer questionId) {
        Subscription subscription = SubscriptionFactory.of().byUser(userId).forQuestion(questionId).make();
        subscriptionMapper.insertSelective(subscription);
        return subscription;
    }

    public void cleanAll() {
        voteMapper.deleteByExample(new com.nofirst.spring.tdd.zhihu.mbg.model.VoteExample());
        subscriptionMapper.deleteByExample(new com.nofirst.spring.tdd.zhihu.mbg.model.SubscriptionExample());
        commentMapper.deleteByExample(new com.nofirst.spring.tdd.zhihu.mbg.model.CommentExample());
        answerMapper.deleteByExample(new com.nofirst.spring.tdd.zhihu.mbg.model.AnswerExample());
        questionMapper.deleteByExample(new com.nofirst.spring.tdd.zhihu.mbg.model.QuestionExample());
    }
}
