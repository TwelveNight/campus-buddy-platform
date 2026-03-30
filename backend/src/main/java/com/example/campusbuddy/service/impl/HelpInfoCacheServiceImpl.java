package com.example.campusbuddy.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.campusbuddy.entity.HelpInfo;
import com.example.campusbuddy.service.HelpInfoCacheService;
import com.example.campusbuddy.vo.HelpInfoDetailVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;

@Slf4j
@Service
public class HelpInfoCacheServiceImpl implements HelpInfoCacheService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String HELP_INFO_LIST_KEY_PREFIX   = "campus:helpinfo:list:";
    private static final String HELP_INFO_DETAIL_KEY_PREFIX = "campus:helpinfo:detail:";
    private static final String HELP_INFO_SEARCH_KEY_PREFIX = "campus:helpinfo:search:";

    @Override
    public void cacheHelpInfoList(String cacheKey, Page<HelpInfo> helpInfoPage, long expireSeconds) {
        try {
            redisTemplate.opsForValue().set(HELP_INFO_LIST_KEY_PREFIX + cacheKey, helpInfoPage, expireSeconds, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("缓存互助信息列表失败: {}", e.getMessage(), e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Page<HelpInfo> getCachedHelpInfoList(String cacheKey) {
        try {
            Object cached = redisTemplate.opsForValue().get(HELP_INFO_LIST_KEY_PREFIX + cacheKey);
            if (cached != null) {
                return (Page<HelpInfo>) cached;
            }
        } catch (Exception e) {
            log.error("获取缓存互助信息列表失败: {}", e.getMessage(), e);
        }
        return null;
    }

    @Override
    public void cacheHelpInfoDetail(Long helpInfoId, HelpInfoDetailVO helpInfoDetail, long expireSeconds) {
        try {
            redisTemplate.opsForValue().set(HELP_INFO_DETAIL_KEY_PREFIX + helpInfoId, helpInfoDetail, expireSeconds, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("缓存互助信息详情失败: {}", e.getMessage(), e);
        }
    }

    @Override
    public HelpInfoDetailVO getCachedHelpInfoDetail(Long helpInfoId) {
        try {
            Object cached = redisTemplate.opsForValue().get(HELP_INFO_DETAIL_KEY_PREFIX + helpInfoId);
            if (cached != null) {
                return (HelpInfoDetailVO) cached;
            }
        } catch (Exception e) {
            log.error("获取缓存互助信息详情失败: {}", e.getMessage(), e);
        }
        return null;
    }

    @Override
    public void clearHelpInfoCache(Long helpInfoId) {
        try {
            redisTemplate.delete(HELP_INFO_DETAIL_KEY_PREFIX + helpInfoId);
        } catch (Exception e) {
            log.error("清除互助信息缓存失败: {}", e.getMessage(), e);
        }
    }

    @Override
    public void clearHelpInfoListCache() {
        clearByPattern(HELP_INFO_LIST_KEY_PREFIX + "*");
        clearByPattern(HELP_INFO_SEARCH_KEY_PREFIX + "*");
    }

    @Override
    public void clearAllHelpInfoCache() {
        try {
            Set<String> allKeys = new HashSet<>();
            addKeys(allKeys, HELP_INFO_LIST_KEY_PREFIX + "*");
            addKeys(allKeys, HELP_INFO_DETAIL_KEY_PREFIX + "*");
            addKeys(allKeys, HELP_INFO_SEARCH_KEY_PREFIX + "*");
            if (!allKeys.isEmpty()) {
                redisTemplate.delete(allKeys);
                log.info("清除所有互助信息缓存成功, 清除键数量: {}", allKeys.size());
            }
        } catch (Exception e) {
            log.error("清除所有互助信息缓存失败: {}", e.getMessage(), e);
        }
    }

    @Override
    public String generateListCacheKey(long page, long size, String type, String status,
            String publisherId, String keyword) {
        StringBuilder sb = new StringBuilder("page:").append(page).append(":size:").append(size);
        if (type != null && !type.trim().isEmpty()) sb.append(":type:").append(type);
        if (status != null && !status.trim().isEmpty()) sb.append(":status:").append(status);
        if (publisherId != null && !publisherId.trim().isEmpty()) sb.append(":publisher:").append(publisherId);
        if (keyword != null && !keyword.trim().isEmpty()) sb.append(":keyword:").append(keyword.hashCode());
        return sb.toString();
    }

    private void clearByPattern(String pattern) {
        try {
            List<String> keys = new ArrayList<>();
            ScanOptions options = ScanOptions.scanOptions().match(pattern).count(200).build();
            try (Cursor<String> cursor = redisTemplate.scan(options)) {
                cursor.forEachRemaining(keys::add);
            }
            if (!keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
        } catch (Exception e) {
            log.error("按模式清除缓存失败: pattern={}", pattern, e);
        }
    }

    private void addKeys(Set<String> target, String pattern) {
        try {
            ScanOptions options = ScanOptions.scanOptions().match(pattern).count(200).build();
            try (Cursor<String> cursor = redisTemplate.scan(options)) {
                cursor.forEachRemaining(target::add);
            }
        } catch (Exception e) {
            log.error("按模式扫描缓存键失败: pattern={}", pattern, e);
        }
    }
}
