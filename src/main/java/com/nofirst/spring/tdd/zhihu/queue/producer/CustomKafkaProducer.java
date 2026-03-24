package com.nofirst.spring.tdd.zhihu.queue.producer;

import com.nofirst.spring.tdd.zhihu.config.TranslatorConfig;
import com.nofirst.spring.tdd.zhihu.event.TranslateSlugEvent;
import com.nofirst.spring.tdd.zhihu.mbg.model.Question;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@Slf4j
@AllArgsConstructor
public class CustomKafkaProducer {

    private KafkaTemplate<String, Object> kafkaTemplate;
    private TranslatorConfig translatorConfig;

    public void sendTranslateEvent(Question question) {
        TranslateSlugEvent translateSlugEvent = new TranslateSlugEvent(question, new Date());
        kafkaTemplate.send(translatorConfig.getTopic(), translateSlugEvent);
        log.info("Sent message: {}", translateSlugEvent);
    }
}