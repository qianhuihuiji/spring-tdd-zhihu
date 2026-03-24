package com.nofirst.spring.tdd.zhihu.model.vo;

import lombok.Data;

import java.util.Date;

@Data
public class AnswerVo extends BaseVoteVo {

    private Integer id;

    private Integer questionId;

    private Integer userId;

    private Date createdAt;

    private Date updatedAt;

    private String content;
}