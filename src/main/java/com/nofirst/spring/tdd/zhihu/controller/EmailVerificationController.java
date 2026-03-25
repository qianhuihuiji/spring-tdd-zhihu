package com.nofirst.spring.tdd.zhihu.controller;

import com.nofirst.spring.tdd.zhihu.common.CommonResult;
import com.nofirst.spring.tdd.zhihu.mbg.mapper.EmailVerificationMapper;
import com.nofirst.spring.tdd.zhihu.mbg.mapper.UserMapper;
import com.nofirst.spring.tdd.zhihu.mbg.model.EmailVerification;
import com.nofirst.spring.tdd.zhihu.mbg.model.EmailVerificationExample;
import com.nofirst.spring.tdd.zhihu.mbg.model.User;
import com.nofirst.spring.tdd.zhihu.util.EmailVerificationUtil;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;

/**
 * 邮箱验证控制器
 */
@RestController
@RequestMapping(path = "/auth", produces = "application/json;charset=utf-8")
@AllArgsConstructor
public class EmailVerificationController {

    private final EmailVerificationMapper emailVerificationMapper;
    private final UserMapper userMapper;

    /**
     * 验证邮箱
     *
     * @param token 验证 Token
     * @return 验证结果
     */
    @GetMapping("/verify-email")
    public CommonResult<String> verifyEmail(@RequestParam String token) {
        // 1. 验证 Token
        EmailVerificationUtil.TokenPayload payload = EmailVerificationUtil.verifyToken(token);
        if (payload == null) {
            return CommonResult.failed("无效的验证链接");
        }

        Integer userId = payload.getUserId();
        String code = payload.getCode();

        // 2. 查询验证码（使用 user_id 查询）
        EmailVerificationExample example = new EmailVerificationExample();
        example.createCriteria().andUserIdEqualTo(userId);
        List<EmailVerification> verifications = emailVerificationMapper.selectByExample(example);
        if (verifications == null || verifications.isEmpty()) {
            return CommonResult.failed("验证码不存在");
        }

        EmailVerification verification = verifications.get(0);

        // 3. 验证验证码是否匹配
        if (!verification.getCode().equals(code)) {
            return CommonResult.failed("验证码错误");
        }

        // 4. 检查是否已验证
        if (verification.getVerifiedAt() != null) {
            return CommonResult.success("邮箱已验证，无需重复验证");
        }

        // 5. 标记验证码为已使用
        verification.setVerifiedAt(new Date());
        emailVerificationMapper.updateByPrimaryKeySelective(verification);

        // 6. 更新用户邮箱验证状态
        User user = userMapper.selectByPrimaryKey(userId);
        if (user != null) {
            user.setEmailVerifiedAt(new Date());
            userMapper.updateByPrimaryKeySelective(user);
        }

        return CommonResult.success("邮箱验证成功");
    }
}
