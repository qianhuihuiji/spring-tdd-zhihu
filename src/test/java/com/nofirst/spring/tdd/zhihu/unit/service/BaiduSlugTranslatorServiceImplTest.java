package com.nofirst.spring.tdd.zhihu.unit.service;

import com.nofirst.spring.tdd.zhihu.config.BaiduTranslatorConfig;
import com.nofirst.spring.tdd.zhihu.service.impl.BaiduTranslatorServiceImpl;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;

// 仅加载配置属性，不扫描任何业务Bean
@ExtendWith({SpringExtension.class, MockitoExtension.class})
// 仅初始化配置相关上下文，指定配置类
@ContextConfiguration(
        classes = BaiduTranslatorConfig.class,
        initializers = ConfigDataApplicationContextInitializer.class // 仅加载配置文件
)
// 启用配置属性绑定
@EnableConfigurationProperties
class BaiduSlugTranslatorServiceImplTest {

    @Autowired
    private BaiduTranslatorConfig translatorConfig;

    @Test
    @Tag("online")
    void can_translate_chinese_to_english() {
        // given
        BaiduTranslatorServiceImpl translatorService = new BaiduTranslatorServiceImpl(translatorConfig);
        String text = "英语 英语";
        // when
        String translate = translatorService.translate(text);
        // then
        assertThat(translate).isEqualTo("english-english");
    }
}
