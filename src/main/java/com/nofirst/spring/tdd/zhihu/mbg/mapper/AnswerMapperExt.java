package com.nofirst.spring.tdd.zhihu.mbg.mapper;

import com.nofirst.spring.tdd.zhihu.model.dto.UserCountDto;

import java.util.Date;
import java.util.List;

public interface AnswerMapperExt {

    List<UserCountDto> countActiveUser(Date beginTime);
}
