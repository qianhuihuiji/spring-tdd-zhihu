package com.nofirst.spring.tdd.zhihu.policy;

import com.nofirst.spring.tdd.zhihu.exception.UserNotExistedException;
import com.nofirst.spring.tdd.zhihu.mbg.mapper.UserMapper;
import com.nofirst.spring.tdd.zhihu.mbg.model.User;
import com.nofirst.spring.tdd.zhihu.security.AccountUser;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@AllArgsConstructor
public class UserPolicy {

    private final UserMapper userMapper;

    public boolean canModifyAvatar(Integer userId, AccountUser accountUser) {
        User user = userMapper.selectByPrimaryKey(userId);
        if (Objects.isNull(user)) {
            throw new UserNotExistedException();
        }

        return accountUser.getUserId().equals(user.getId());
    }
}
