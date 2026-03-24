package com.nofirst.spring.tdd.zhihu.model.dto;

import lombok.Data;

@Data
public class VoteCountDto {

    private Integer resourceId;

    private Integer voteCount;
}
