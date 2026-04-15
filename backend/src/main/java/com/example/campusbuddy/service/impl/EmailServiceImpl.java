package com.example.campusbuddy.service.impl;

import com.example.campusbuddy.mapper.UserMapper;
import com.example.campusbuddy.service.EmailService;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private UserMapper userMapper;

    @Value("${email.from:your_email@qq.com}")
    private String fromEmail;

    @Value("${email.code-expire-seconds:300}")
    private long codeExpireSeconds;

    /** Redis key 前缀：email:code:{codeType}:{email} */
    private static final String CODE_KEY_PREFIX = "campus:email:code:";

    @Override
    public void sendVerifyCode(String email, String codeType) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("邮箱不能为空");
        }

        // 注册场景：发验证码前先检查邮箱是否已被注册，避免浪费邮件资源
        if ("REGISTER".equalsIgnoreCase(codeType)) {
            if (userMapper.findByEmail(email) != null) {
                throw new IllegalArgumentException("该邮箱已被注册，请直接登录或使用其他邮箱");
            }
        }

        // 登录场景：检查邮箱是否已绑定账号
        if ("LOGIN".equalsIgnoreCase(codeType)) {
            if (userMapper.findByEmail(email) == null) {
                throw new IllegalArgumentException("该邮箱尚未绑定任何账号，请先注册");
            }
        }

        // 生成 6 位数字验证码
        String code = String.format("%06d", new Random().nextInt(1_000_000));

        // 存入 Redis，TTL = codeExpireSeconds
        String redisKey = CODE_KEY_PREFIX + codeType + ":" + email;
        redisTemplate.opsForValue().set(redisKey, code, codeExpireSeconds, TimeUnit.SECONDS);

        // 发送邮件
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(email);
            helper.setSubject("【学伴平台】邮箱验证码");
            String htmlContent = buildEmailHtml(code, codeType, codeExpireSeconds / 60);
            helper.setText(htmlContent, true);
            mailSender.send(message);
            log.info("邮箱验证码已发送: email={}, codeType={}", email, codeType);
        } catch (Exception e) {
            // 发送失败时清除 Redis 中的验证码，避免用户无法重试
            redisTemplate.delete(redisKey);
            log.error("邮件发送失败: email={}", email, e);
            throw new RuntimeException("邮件发送失败，请检查邮箱地址是否正确或稍后重试");
        }
    }

    @Override
    public boolean verifyCode(String email, String codeType, String code) {
        if (email == null || codeType == null || code == null) {
            return false;
        }
        String redisKey = CODE_KEY_PREFIX + codeType + ":" + email;
        Object stored = redisTemplate.opsForValue().get(redisKey);
        if (stored == null) {
            return false;
        }
        boolean match = code.trim().equals(stored.toString().trim());
        if (match) {
            // 验证成功后立即删除，保证一次性使用
            redisTemplate.delete(redisKey);
        }
        return match;
    }

    private String buildEmailHtml(String code, String codeType, long expireMinutes) {
        String purpose = "LOGIN".equalsIgnoreCase(codeType) ? "登录" : "注册";
        return "<div style='font-family:Arial,sans-serif;max-width:480px;margin:0 auto;padding:32px;border:1px solid #e0e0e0;border-radius:8px;'>"
                + "<h2 style='color:#4a90e2;margin-bottom:8px;'>学伴校园平台</h2>"
                + "<p style='color:#555;'>您正在进行<b>" + purpose + "</b>操作，您的验证码为：</p>"
                + "<div style='font-size:36px;font-weight:bold;letter-spacing:8px;color:#333;"
                + "text-align:center;padding:16px 0;background:#f5f7fa;border-radius:6px;margin:16px 0;'>"
                + code
                + "</div>"
                + "<p style='color:#888;font-size:13px;'>验证码有效期 <b>" + expireMinutes + " 分钟</b>，请勿泄露给他人。</p>"
                + "<p style='color:#bbb;font-size:12px;margin-top:24px;'>如非本人操作，请忽略此邮件。</p>"
                + "</div>";
    }
}
