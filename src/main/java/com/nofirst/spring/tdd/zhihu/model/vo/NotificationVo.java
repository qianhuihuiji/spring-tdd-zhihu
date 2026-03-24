package com.nofirst.spring.tdd.zhihu.model.vo;

import lombok.Data;

import java.util.Date;


@Data
public class NotificationVo {

    private Integer id;

    private String type;

    private Integer userId;


    private Date readAt;

    private Date createdAt;

    private Date updatedAt;
}