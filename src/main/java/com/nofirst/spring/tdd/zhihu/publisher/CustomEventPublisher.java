package com.nofirst.spring.tdd.zhihu.publisher;

import com.nofirst.spring.tdd.zhihu.event.PostAnswerEvent;
import com.nofirst.spring.tdd.zhihu.event.PostCommentEvent;
import com.nofirst.spring.tdd.zhihu.event.PublishQuestionEvent;
import com.nofirst.spring.tdd.zhihu.mbg.model.Answer;
import com.nofirst.spring.tdd.zhihu.mbg.model.Comment;
import com.nofirst.spring.tdd.zhihu.mbg.model.Question;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
public class CustomEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    public CustomEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    public void firePublishQuestionEvent(Question question) {
        PublishQuestionEvent publishQuestionEvent = new PublishQuestionEvent(question);
        applicationEventPublisher.publishEvent(publishQuestionEvent);
    }

    public void firePostAnswerEvent(Answer answer, Integer userId) {
        PostAnswerEvent postAnswerEvent = new PostAnswerEvent(answer, userId);
        applicationEventPublisher.publishEvent(postAnswerEvent);
    }

    public void firePostCommentEvent(Comment comment) {
        PostCommentEvent postCommentEvent = new PostCommentEvent(comment);
        applicationEventPublisher.publishEvent(postCommentEvent);
    }
}
