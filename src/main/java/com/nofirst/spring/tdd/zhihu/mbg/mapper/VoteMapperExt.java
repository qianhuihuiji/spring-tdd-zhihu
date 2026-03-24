package com.nofirst.spring.tdd.zhihu.mbg.mapper;


import com.nofirst.spring.tdd.zhihu.model.dto.VoteCountDto;

import java.util.List;


public interface VoteMapperExt {

    List<VoteCountDto> countByResource(String resourceType, Byte voteType, List<Integer> answerIds);
}
