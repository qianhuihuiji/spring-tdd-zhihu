package com.nofirst.spring.tdd.zhihu.service;

import com.github.pagehelper.PageInfo;
import com.nofirst.spring.tdd.zhihu.model.dto.CommentDto;
import com.nofirst.spring.tdd.zhihu.model.vo.CommentVo;
import com.nofirst.spring.tdd.zhihu.security.AccountUser;

public interface CommentService {

    void comment(Integer commentedId, String commentedType, CommentDto commentDto, AccountUser accountUser);

    PageInfo<CommentVo> index(Integer commentedId, String commentedType, Integer pageIndex, Integer pageSize, AccountUser accountUser);
}