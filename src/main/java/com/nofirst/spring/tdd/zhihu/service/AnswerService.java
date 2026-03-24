package com.nofirst.spring.tdd.zhihu.service;


import com.github.pagehelper.PageInfo;
import com.nofirst.spring.tdd.zhihu.model.dto.AnswerDto;
import com.nofirst.spring.tdd.zhihu.model.vo.AnswerVo;
import com.nofirst.spring.tdd.zhihu.security.AccountUser;

public interface AnswerService {

    PageInfo<AnswerVo> answers(Integer questionId, int pageIndex, int pageSize, AccountUser accountUser);

    void store(Integer questionId, AnswerDto answerDto, AccountUser accountUser);

    void markAsBest(Integer answerId);

    void destroy(Integer answerId);
}
