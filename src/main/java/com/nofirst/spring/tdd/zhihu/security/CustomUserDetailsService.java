package com.nofirst.spring.tdd.zhihu.security;

import com.nofirst.spring.tdd.zhihu.mbg.mapper.UserMapperExt;
import com.nofirst.spring.tdd.zhihu.mbg.model.User;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private UserMapperExt userMapperExt;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 1. 从数据库查询用户
        User user = userMapperExt.selectByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在");
        }
        // 2. 检查邮箱是否已验证
        if (user.getEmailVerifiedAt() == null) {
            // 抛出认证异常，返回 403
            throw new EmailNotVerifiedException("请先验证邮箱");
        }
        // 3. 封装用户信息（注意参数顺序：userId, password, username）
        return new AccountUser(user.getId(), user.getPassword(), user.getName());
    }
}
