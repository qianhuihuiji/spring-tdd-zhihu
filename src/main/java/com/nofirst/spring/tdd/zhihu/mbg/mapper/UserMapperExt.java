package com.nofirst.spring.tdd.zhihu.mbg.mapper;


import com.nofirst.spring.tdd.zhihu.mbg.model.User;


public interface UserMapperExt {

    User selectByUsername(String username);
}
