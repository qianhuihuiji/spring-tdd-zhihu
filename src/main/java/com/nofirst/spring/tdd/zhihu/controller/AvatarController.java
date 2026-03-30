package com.nofirst.spring.tdd.zhihu.controller;

import com.nofirst.spring.tdd.zhihu.common.CommonResult;
import com.nofirst.spring.tdd.zhihu.mbg.mapper.UserMapper;
import com.nofirst.spring.tdd.zhihu.mbg.model.User;
import com.nofirst.spring.tdd.zhihu.model.vo.UserVo;
import com.nofirst.spring.tdd.zhihu.service.AvatarService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping(path = "/users", produces = "application/json;charset=utf-8")
@AllArgsConstructor
public class AvatarController {

    private final AvatarService avatarService;
    private final UserMapper userMapper;

    @PostMapping(path = "/{id}/avatar", consumes = "multipart/form-data")
    public CommonResult<String> uploadAvatar(@PathVariable Integer id,
                                             @RequestParam("file") MultipartFile file) {
        try {
            String relativePath = avatarService.saveAvatar(id, file);
            return CommonResult.success(relativePath);
        } catch (IllegalArgumentException e) {
            return CommonResult.failed(e.getMessage());
        } catch (IOException e) {
            return CommonResult.failed("failed to save file");
        }
    }

    @GetMapping(path = "/{id}")
    public CommonResult<UserVo> getUser(@PathVariable Integer id) {
        User user = userMapper.selectByPrimaryKey(id);
        if (user == null) {
            return CommonResult.failed("user not found");
        }
        UserVo vo = new UserVo();
        vo.setId(user.getId());
        vo.setName(user.getName());
        vo.setEmail(user.getEmail());
        vo.setPhone(user.getPhone());
        vo.setAvatar(user.getAvatar());
        return CommonResult.success(vo);
    }
}
