package com.example.campusbuddy.service;

/**
 * 邮箱验证码服务
 */
public interface EmailService {

    /**
     * 发送邮箱验证码
     *
     * @param email    目标邮箱
     * @param codeType 验证码类型：LOGIN | REGISTER
     */
    void sendVerifyCode(String email, String codeType);

    /**
     * 校验邮箱验证码（校验通过后自动删除，验证码一次性有效）
     *
     * @param email    邮箱
     * @param codeType 验证码类型
     * @param code     用户输入的验证码
     * @return 是否匹配
     */
    boolean verifyCode(String email, String codeType, String code);
}
