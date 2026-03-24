package com.nofirst.spring.tdd.zhihu.controller;

import com.nofirst.spring.tdd.zhihu.common.CommonResult;
import com.nofirst.spring.tdd.zhihu.mbg.mapper.UserMapper;
import com.nofirst.spring.tdd.zhihu.mbg.model.User;
import com.nofirst.spring.tdd.zhihu.mbg.model.UserExample;
import com.nofirst.spring.tdd.zhihu.model.dto.UserLoginDto;
import com.nofirst.spring.tdd.zhihu.model.dto.UserRegisterDto;
import com.nofirst.spring.tdd.zhihu.security.AccountUser;
import com.nofirst.spring.tdd.zhihu.security.JwtUtil;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;


@RestController
@RequestMapping(path = "/auth", produces = "application/json;charset=utf-8")
@AllArgsConstructor
public class UserController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;


    @PostMapping("/register")
    public CommonResult<String> register(@RequestBody @Validated UserRegisterDto registerDto) {
        // 1. 检查用户名是否已存在（使用数据库查询，避免全表加载到内存）
        UserExample example = new UserExample();
        example.createCriteria().andNameEqualTo(registerDto.getName());
        List<User> users = userMapper.selectByExample(example);
        if (!users.isEmpty()) {
            return CommonResult.failed("用户名已存在");
        }

        // 2. 创建新用户，密码使用 BCrypt 加密
        User user = new User();
        user.setName(registerDto.getName());
        user.setPhone(registerDto.getPhone());
        user.setEmail(registerDto.getEmail());
        user.setPassword(passwordEncoder.encode(registerDto.getPassword()));
        user.setCreatedAt(new Date());
        user.setUpdatedAt(new Date());

        // 3. 插入数据库
        userMapper.insertSelective(user);

        return CommonResult.success("注册成功");
    }

    @PostMapping("/login")
    public CommonResult<String> login(@RequestBody @Validated UserLoginDto loginDTO) {
        // 1. 执行认证（用户名密码校验）
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDTO.getUsername(), loginDTO.getPassword())
        );
        // 2. 认证成功，生成 Token
        AccountUser accountUser = (AccountUser) authentication.getPrincipal();
        String username = accountUser.getUsername();
        Integer userId = accountUser.getUserId();
        String token = jwtUtil.generateToken(userId, username);
        // 3. 返回 Token 给前端
        return CommonResult.success(token);
    }

    @GetMapping("/logout")
    public CommonResult<String> logout() {
        // JWT 是无状态的，服务端不保存会话信息
        // 前端调用此接口后，自行删除本地存储的 Token 即可
        SecurityContextHolder.clearContext();
        return CommonResult.success("退出登录成功");
    }
}