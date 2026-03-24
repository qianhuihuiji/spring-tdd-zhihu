package com.nofirst.spring.tdd.zhihu.mbg.mapper;

import com.nofirst.spring.tdd.zhihu.model.dto.UserCountDto;

import java.util.Date;
import java.util.List;

public interface QuestionMapperExt {

    void markAsBestAnswer(Integer questionId, Integer answerId);

    List<UserCountDto> countActiveUser(Date beginTime);
}
