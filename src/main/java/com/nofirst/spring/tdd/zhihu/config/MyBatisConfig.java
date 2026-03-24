package com.nofirst.spring.tdd.zhihu.config;


import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis配置类
 */
@Configuration
@MapperScan({"com.nofirst.spring.tdd.zhihu.mbg.mapper"})
public class MyBatisConfig {
}


