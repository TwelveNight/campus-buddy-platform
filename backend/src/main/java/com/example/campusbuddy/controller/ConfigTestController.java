package com.example.campusbuddy.controller;

import com.example.campusbuddy.common.R;
import com.example.campusbuddy.config.QiniuConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 配置测试控制器
 */
@RestController
@RequestMapping("/api/test/config")
public class ConfigTestController {

    @Autowired
    private QiniuConfig.QiniuProperties qiniuProperties;

    /**
     * 获取七牛云配置（安全起见，只返回部分信息）
     */
    @GetMapping("/qiniu")
    public R<Map<String, String>> getQiniuConfig() {
        Map<String, String> config = new HashMap<>();
        // 不返回敏感信息，只返回部分信息用于验证
        config.put("accessKeyPart", maskString(qiniuProperties.getAccessKey()));
        config.put("secretKeyPart", maskString(qiniuProperties.getSecretKey()));
        config.put("bucket", qiniuProperties.getBucket());
        config.put("domain", qiniuProperties.getDomain());
        // 也添加环境变量信息
        config.put("ENV_QINIU_ACCESS_KEY", maskString(System.getenv("QINIU_ACCESS_KEY")));
        config.put("ENV_QINIU_SECRET_KEY", maskString(System.getenv("QINIU_SECRET_KEY")));
        config.put("ENV_QINIU_BUCKET", System.getenv("QINIU_BUCKET"));
        config.put("ENV_QINIU_DOMAIN", System.getenv("QINIU_DOMAIN"));
        
        return R.ok(config);
    }
    
    /**
     * 敏感信息掩码，只显示前后几个字符
     */
    private String maskString(String input) {
        if (input == null || input.length() <= 6) {
            return "***"; // 太短的直接全部掩码
        }
        return input.substring(0, 3) + "***" + input.substring(input.length() - 3);
    }
}
