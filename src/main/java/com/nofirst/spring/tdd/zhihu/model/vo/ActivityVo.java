package com.nofirst.spring.tdd.zhihu.model.vo;

import lombok.Data;

import java.util.Date;

@Data
public class ActivityVo {
    private Integer id;
    private Integer userId;
    private String type;
    private Integer subjectId;
    private String subjectType;
    private Date createdAt;
}
