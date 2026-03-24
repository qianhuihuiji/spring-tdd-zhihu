package com.nofirst.spring.tdd.zhihu.queue.consumer;

import com.nofirst.spring.tdd.zhihu.event.TranslateSlugEvent;
import com.nofirst.spring.tdd.zhihu.mbg.mapper.QuestionMapper;
import com.nofirst.spring.tdd.zhihu.mbg.model.Question;
import com.nofirst.spring.tdd.zhihu.service.TranslatorService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@AllArgsConstructor
@Slf4j
public class TranslateSlugEventConsumer {

    private TranslatorService translatorService;
    private QuestionMapper questionMapper;

    @KafkaListener(topics = "${translate.kafka.topic}", groupId = "${translate.kafka.group}")
    public void listen(TranslateSlugEvent translateSlugEvent) {
        log.info("Received: {}", translateSlugEvent);
        Question question = translateSlugEvent.getQuestion();
        String translatedSlug = translatorService.translate(question.getTitle());

        Question updateQuestion = new Question();
        updateQuestion.setId(question.getId());
        updateQuestion.setSlug(translatedSlug);
        updateQuestion.setUpdatedAt(new Date());

        questionMapper.updateByPrimaryKeySelective(updateQuestion);
    }
}
