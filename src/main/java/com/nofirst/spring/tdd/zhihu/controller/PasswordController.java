package com.nofirst.spring.tdd.zhihu.controller;

import com.nofirst.spring.tdd.zhihu.common.CommonResult;
import com.nofirst.spring.tdd.zhihu.mbg.mapper.UserMapper;
import com.nofirst.spring.tdd.zhihu.mbg.model.User;
import com.nofirst.spring.tdd.zhihu.model.dto.ChangePasswordDto;
import com.nofirst.spring.tdd.zhihu.security.AccountUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

@Tag(name = "密码管理")
@RestController
@RequestMapping(path = "/users", produces = "application/json;charset=utf-8")
@AllArgsConstructor
public class PasswordController {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Operation(summary = "修改密码")
    @PostMapping("/me/password")
    public CommonResult<String> changePassword(
            @RequestBody @Validated ChangePasswordDto dto,
            @AuthenticationPrincipal AccountUser accountUser) {

        User user = userMapper.selectByPrimaryKey(accountUser.getUserId());
        if (user == null) {
            return CommonResult.failed("用户不存在");
        }

        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            return CommonResult.failed("原密码不正确");
        }

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        user.setUpdatedAt(new Date());
        userMapper.updateByPrimaryKeySelective(user);

        return CommonResult.success("密码修改成功");
    }
}
