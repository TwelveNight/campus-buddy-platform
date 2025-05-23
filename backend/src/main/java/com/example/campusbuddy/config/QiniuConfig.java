package com.example.campusbuddy.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 七牛云配置类
 * 确保环境变量优先级更高
 */
@Configuration
public class QiniuConfig {

    private static final Logger log = LoggerFactory.getLogger(QiniuConfig.class);

    /**
     * 创建七牛云配置bean，优先从环境变量中读取
     */
    @Bean
    @ConfigurationProperties(prefix = "qiniu")
    public QiniuProperties qiniuProperties() {
        QiniuProperties properties = new QiniuProperties();

        // 优先从环境变量中读取
        String accessKey = System.getenv("QINIU_ACCESS_KEY");
        log.info("环境变量QINIU_ACCESS_KEY: {}", maskString(accessKey));

        String secretKey = System.getenv("QINIU_SECRET_KEY");
        log.info("环境变量QINIU_SECRET_KEY: {}", maskString(secretKey));

        String bucket = System.getenv("QINIU_BUCKET");
        log.info("环境变量QINIU_BUCKET: {}", bucket);

        String domain = System.getenv("QINIU_DOMAIN");
        log.info("环境变量QINIU_DOMAIN: {}", domain);

        // 如果环境变量存在，则使用环境变量值
        if (accessKey != null && !accessKey.isEmpty()) {
            properties.setAccessKey(accessKey);
            log.info("使用环境变量的accessKey");
        } else {
            log.warn("未找到环境变量QINIU_ACCESS_KEY，使用默认值");
        }

        if (secretKey != null && !secretKey.isEmpty()) {
            properties.setSecretKey(secretKey);
            log.info("使用环境变量的secretKey");
        } else {
            log.warn("未找到环境变量QINIU_SECRET_KEY，使用默认值");
        }

        if (bucket != null && !bucket.isEmpty()) {
            properties.setBucket(bucket);
            log.info("使用环境变量的bucket: {}", bucket);
        } else {
            log.warn("未找到环境变量QINIU_BUCKET，使用默认值: {}", properties.getBucket());
        }

        if (domain != null && !domain.isEmpty()) {
            properties.setDomain(domain);
            log.info("使用环境变量的domain: {}", domain);
        } else {
            log.warn("未找到环境变量QINIU_DOMAIN，使用默认值: {}", properties.getDomain());
        }

        // 记录最终使用的属性值
        log.info("最终使用的七牛云配置:");
        log.info("qiniuProperties.accessKey: {}", maskString(properties.getAccessKey()));
        log.info("qiniuProperties.secretKey: {}", maskString(properties.getSecretKey()));
        log.info("qiniuProperties.bucket: {}", properties.getBucket());
        log.info("qiniuProperties.domain: {}", properties.getDomain());

        return properties;
    }

    // 掩码敏感信息，只显示前后3个字符
    private String maskString(String input) {
        if (input == null) {
            return null;
        }
        if (input.length() <= 6) {
            return "***";
        }
        return input.substring(0, 3) + "***" + input.substring(input.length() - 3);
    }

    /**
     * 七牛云配置属性类
     */
    public static class QiniuProperties {
        private String accessKey;
        private String secretKey;
        private String bucket;
        private String domain;

        public String getAccessKey() {
            return accessKey;
        }

        public void setAccessKey(String accessKey) {
            this.accessKey = accessKey;
        }

        public String getSecretKey() {
            return secretKey;
        }

        public void setSecretKey(String secretKey) {
            this.secretKey = secretKey;
        }

        public String getBucket() {
            return bucket;
        }

        public void setBucket(String bucket) {
            this.bucket = bucket;
        }

        public String getDomain() {
            return domain;
        }

        public void setDomain(String domain) {
            this.domain = domain;
        }
    }
}
