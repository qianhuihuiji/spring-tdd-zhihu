package com.nofirst.spring.tdd.zhihu.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "translate.kafka")
public class TranslatorConfig {

    private String topic;

    private String group;
}