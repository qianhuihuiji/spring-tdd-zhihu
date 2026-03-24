package com.nofirst.spring.tdd.zhihu.service;

import com.github.pagehelper.PageInfo;
import com.nofirst.spring.tdd.zhihu.model.vo.NotificationVo;
import com.nofirst.spring.tdd.zhihu.security.AccountUser;

public interface UserNotificationService {

    PageInfo<NotificationVo> index(Integer userId, Integer pageIndex, Integer pageSize, AccountUser accountUser);
}