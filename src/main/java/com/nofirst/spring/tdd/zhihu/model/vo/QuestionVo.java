package com.nofirst.spring.tdd.zhihu.model.vo;

import com.github.pagehelper.PageInfo;
import lombok.Data;

@Data
public class QuestionVo extends BaseVoteVo {

    private Integer id;
    private Integer userId;
    private String title;
    private String content;
    private Integer answersCount;

    private PageInfo<AnswerVo> answers;
}