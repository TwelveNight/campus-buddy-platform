package com.example.campusbuddy.config;

import com.example.campusbuddy.service.HelpInfoCacheService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * 缓存任务调度器，用于定期执行缓存相关任务
 */
@Slf4j
@Configuration
@EnableScheduling
public class CacheTaskScheduler {

    @Autowired
    private HelpInfoCacheService helpInfoCacheService;

    /**
     * 每小时清理过期的互助信息缓存
     * 每小时执行一次
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void scheduleExpiredCacheClear() {
        try {
            log.info("开始执行定时任务：清理过期的互助信息缓存");
            helpInfoCacheService.clearAllExpiredCaches();
        } catch (Exception e) {
            log.error("定时清理过期缓存失败: {}", e.getMessage(), e);
        }
    }
}
