package com.nofirst.spring.tdd.zhihu.model.vo;

import lombok.Data;

import java.util.Date;

@Data
public class CommentVo extends BaseVoteVo {

    private Integer id;

    private Integer commentedId;

    private String content;

    private Date createTime;
}
