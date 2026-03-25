package com.nofirst.spring.tdd.zhihu.service;

/**
 * 邮箱验证码服务接口
 */
public interface EmailVerificationService {

    /**
     * 保存验证码
     *
     * @param userId 用户 ID
     * @param email  邮箱地址
     * @param code   验证码
     */
    void saveVerificationCode(Integer userId, String email, String code);

    /**
     * 获取验证码
     *
     * @param userId 用户 ID
     * @return 验证码，不存在返回 null
     */
    String getVerificationCode(Integer userId);

    /**
     * 标记验证码为已使用
     *
     * @param userId 用户 ID
     */
    void markAsVerified(Integer userId);
}
