package com.nofirst.spring.tdd.zhihu.service;

import com.nofirst.spring.tdd.zhihu.mbg.model.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    public User getProfile(Long id) {
        return this.findById(id);
    }

    @Transactional
    public User findById(Long id) {
        return null;
    }
}