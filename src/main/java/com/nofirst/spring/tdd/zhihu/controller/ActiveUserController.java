package com.nofirst.spring.tdd.zhihu.controller;

import com.nofirst.spring.tdd.zhihu.common.CommonResult;
import com.nofirst.spring.tdd.zhihu.model.vo.UserVo;
import com.nofirst.spring.tdd.zhihu.task.ActiveUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "活跃用户")
@RestController
@AllArgsConstructor
public class ActiveUserController {

    private final ActiveUserService activeUserService;

    @Operation(summary = "获取活跃用户列表")
    @GetMapping("/active-users")
    public CommonResult<List<UserVo>> index() {
        List<UserVo> activeUsers = activeUserService.getActiveUsers();
        return CommonResult.success(activeUsers);
    }
}
