package com.nofirst.spring.tdd.zhihu.controller;

import com.nofirst.spring.tdd.zhihu.common.CommonResult;
import com.nofirst.spring.tdd.zhihu.mbg.mapper.EmailVerificationMapper;
import com.nofirst.spring.tdd.zhihu.mbg.mapper.UserMapper;
import com.nofirst.spring.tdd.zhihu.mbg.model.EmailVerification;
import com.nofirst.spring.tdd.zhihu.mbg.model.EmailVerificationExample;
import com.nofirst.spring.tdd.zhihu.mbg.model.User;
import com.nofirst.spring.tdd.zhihu.util.EmailVerificationUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;

@Tag(name = "邮箱验证")
@RestController
@RequestMapping(path = "/auth", produces = "application/json;charset=utf-8")
@AllArgsConstructor
public class EmailVerificationController {

    private final EmailVerificationMapper emailVerificationMapper;
    private final UserMapper userMapper;

    @Operation(summary = "验证邮箱")
    @GetMapping("/verify-email")
    public CommonResult<String> verifyEmail(@RequestParam String token) {
        EmailVerificationUtil.TokenPayload payload = EmailVerificationUtil.verifyToken(token);
        if (payload == null) {
            return CommonResult.failed("无效的验证链接");
        }

        Integer userId = payload.getUserId();
        String code = payload.getCode();

        EmailVerificationExample example = new EmailVerificationExample();
        example.createCriteria().andUserIdEqualTo(userId);
        List<EmailVerification> verifications = emailVerificationMapper.selectByExample(example);
        if (verifications == null || verifications.isEmpty()) {
            return CommonResult.failed("验证码不存在");
        }

        EmailVerification verification = verifications.get(0);

        if (!verification.getCode().equals(code)) {
            return CommonResult.failed("验证码错误");
        }

        if (verification.getVerifiedAt() != null) {
            return CommonResult.success("邮箱已验证，无需重复验证");
        }

        verification.setVerifiedAt(new Date());
        emailVerificationMapper.updateByPrimaryKeySelective(verification);

        User user = userMapper.selectByPrimaryKey(userId);
        if (user != null) {
            user.setEmailVerifiedAt(new Date());
            userMapper.updateByPrimaryKeySelective(user);
        }

        return CommonResult.success("邮箱验证成功");
    }
}
