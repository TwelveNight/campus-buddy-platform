package com.example.campusbuddy.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.campusbuddy.entity.HelpInfo;
import com.example.campusbuddy.entity.User;
import com.example.campusbuddy.mapper.HelpInfoMapper;
import com.example.campusbuddy.service.HelpInfoCacheService;
import com.example.campusbuddy.vo.HelpInfoDetailVO;
import com.example.campusbuddy.vo.HelpInfoVO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 互助信息缓存服务实现
 */
@Slf4j
@Service
public class HelpInfoCacheServiceImpl implements HelpInfoCacheService {
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private HelpInfoMapper helpInfoMapper;
    
    // 缓存键前缀
    private static final String HELP_INFO_LIST_KEY_PREFIX = "campus:helpinfo:list:";
    private static final String HELP_INFO_DETAIL_KEY_PREFIX = "campus:helpinfo:detail:";
    private static final String HELP_INFO_USER_KEY_PREFIX = "campus:helpinfo:user:";
    private static final String HELP_INFO_ADMIN_KEY_PREFIX = "campus:helpinfo:admin:";
    private static final String HELP_INFO_SEARCH_KEY_PREFIX = "campus:helpinfo:search:";
    
    @Override
    public void cacheHelpInfoList(String cacheKey, Page<HelpInfo> helpInfoPage, long expireSeconds) {
        try {
            String key = HELP_INFO_LIST_KEY_PREFIX + cacheKey;
            redisTemplate.opsForValue().set(key, helpInfoPage, expireSeconds, TimeUnit.SECONDS);
            log.info("缓存互助信息列表成功, key: {}", key);
        } catch (Exception e) {
            log.error("缓存互助信息列表失败: {}", e.getMessage(), e);
        }
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public Page<HelpInfo> getCachedHelpInfoList(String cacheKey) {
        try {
            String key = HELP_INFO_LIST_KEY_PREFIX + cacheKey;
            Object cached = redisTemplate.opsForValue().get(key);
            if (cached != null) {
                log.info("获取缓存互助信息列表成功, key: {}", key);
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
            String key = HELP_INFO_DETAIL_KEY_PREFIX + helpInfoId;
            redisTemplate.opsForValue().set(key, helpInfoDetail, expireSeconds, TimeUnit.SECONDS);
            log.debug("缓存互助信息详情成功, id: {}", helpInfoId);
        } catch (Exception e) {
            log.error("缓存互助信息详情失败: {}", e.getMessage(), e);
        }
    }
    
    @Override
    public HelpInfoDetailVO getCachedHelpInfoDetail(Long helpInfoId) {
        try {
            String key = HELP_INFO_DETAIL_KEY_PREFIX + helpInfoId;
            Object cached = redisTemplate.opsForValue().get(key);
            if (cached != null) {
                return (HelpInfoDetailVO) cached;
            }
        } catch (Exception e) {
            log.error("获取缓存互助信息详情失败: {}", e.getMessage(), e);
        }
        return null;
    }
    
    @Override
    public void cacheUser(Long userId, User user, long expireSeconds) {
        try {
            String key = HELP_INFO_USER_KEY_PREFIX + userId;
            redisTemplate.opsForValue().set(key, user, expireSeconds, TimeUnit.SECONDS);
            log.debug("缓存用户信息成功, id: {}", userId);
        } catch (Exception e) {
            log.error("缓存用户信息失败: {}", e.getMessage(), e);
        }
    }
    
    @Override
    public void cacheUsers(Map<Long, User> userMap, long expireSeconds) {
        try {
            Map<String, Object> pipeline = new HashMap<>();
            for (Map.Entry<Long, User> entry : userMap.entrySet()) {
                String key = HELP_INFO_USER_KEY_PREFIX + entry.getKey();
                pipeline.put(key, entry.getValue());
            }
            
            redisTemplate.opsForValue().multiSet(pipeline);
            
            // 为每个键设置过期时间
            for (String key : pipeline.keySet()) {
                redisTemplate.expire(key, expireSeconds, TimeUnit.SECONDS);
            }
            
            log.debug("批量缓存用户信息成功, 数量: {}", userMap.size());
        } catch (Exception e) {
            log.error("批量缓存用户信息失败: {}", e.getMessage(), e);
        }
    }
    
    @Override
    public User getCachedUser(Long userId) {
        try {
            String key = HELP_INFO_USER_KEY_PREFIX + userId;
            Object cached = redisTemplate.opsForValue().get(key);
            if (cached != null) {
                return (User) cached;
            }
        } catch (Exception e) {
            log.error("获取缓存用户信息失败: {}", e.getMessage(), e);
        }
        return null;
    }
    
    @Override
    public Map<Long, User> getCachedUsers(List<Long> userIds) {
        Map<Long, User> result = new HashMap<>();
        try {
            List<String> keys = userIds.stream()
                    .map(id -> HELP_INFO_USER_KEY_PREFIX + id)
                    .collect(Collectors.toList());
            
            List<Object> cached = redisTemplate.opsForValue().multiGet(keys);
            
            for (int i = 0; i < userIds.size(); i++) {
                if (cached.get(i) != null) {
                    result.put(userIds.get(i), (User) cached.get(i));
                }
            }
            
            log.debug("批量获取缓存用户信息成功, 请求: {}, 命中: {}", userIds.size(), result.size());
        } catch (Exception e) {
            log.error("批量获取缓存用户信息失败: {}", e.getMessage(), e);
        }
        return result;
    }
    
    @Override
    public void cacheAdminHelpInfoList(String cacheKey, Page<HelpInfoVO> helpInfoPage, long expireSeconds) {
        try {
            String key = HELP_INFO_ADMIN_KEY_PREFIX + cacheKey;
            redisTemplate.opsForValue().set(key, helpInfoPage, expireSeconds, TimeUnit.SECONDS);
            log.debug("缓存管理员查询结果成功, key: {}", key);
        } catch (Exception e) {
            log.error("缓存管理员查询结果失败: {}", e.getMessage(), e);
        }
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public Page<HelpInfoVO> getCachedAdminHelpInfoList(String cacheKey) {
        try {
            String key = HELP_INFO_ADMIN_KEY_PREFIX + cacheKey;
            Object cached = redisTemplate.opsForValue().get(key);
            if (cached != null) {
                return (Page<HelpInfoVO>) cached;
            }
        } catch (Exception e) {
            log.error("获取缓存管理员查询结果失败: {}", e.getMessage(), e);
        }
        return null;
    }
    
    @Override
    public void cacheSearchResults(String searchKey, Page<HelpInfo> helpInfoPage, long expireSeconds) {
        try {
            String key = HELP_INFO_SEARCH_KEY_PREFIX + searchKey;
            redisTemplate.opsForValue().set(key, helpInfoPage, expireSeconds, TimeUnit.SECONDS);
            log.debug("缓存搜索结果成功, key: {}", key);
        } catch (Exception e) {
            log.error("缓存搜索结果失败: {}", e.getMessage(), e);
        }
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public Page<HelpInfo> getCachedSearchResults(String searchKey) {
        try {
            String key = HELP_INFO_SEARCH_KEY_PREFIX + searchKey;
            Object cached = redisTemplate.opsForValue().get(key);
            if (cached != null) {
                return (Page<HelpInfo>) cached;
            }
        } catch (Exception e) {
            log.error("获取缓存搜索结果失败: {}", e.getMessage(), e);
        }
        return null;
    }
    

    
    @Override
    public void clearHelpInfoCache(Long helpInfoId) {
        try {
            // 清除详情缓存
            String detailKey = HELP_INFO_DETAIL_KEY_PREFIX + helpInfoId;
            redisTemplate.delete(detailKey);
            
            log.debug("清除互助信息缓存成功, id: {}", helpInfoId);
        } catch (Exception e) {
            log.error("清除互助信息缓存失败: {}", e.getMessage(), e);
        }
    }
    
    @Override
    public void clearHelpInfoListCache() {
        try {
            // 清除列表缓存
            Set<String> listKeys = redisTemplate.keys(HELP_INFO_LIST_KEY_PREFIX + "*");
            if (listKeys != null && !listKeys.isEmpty()) {
                redisTemplate.delete(listKeys);
            }
            
            // 清除搜索缓存
            Set<String> searchKeys = redisTemplate.keys(HELP_INFO_SEARCH_KEY_PREFIX + "*");
            if (searchKeys != null && !searchKeys.isEmpty()) {
                redisTemplate.delete(searchKeys);
            }
            
            // 清除管理员查询缓存
            Set<String> adminKeys = redisTemplate.keys(HELP_INFO_ADMIN_KEY_PREFIX + "*");
            if (adminKeys != null && !adminKeys.isEmpty()) {
                redisTemplate.delete(adminKeys);
            }
            
            log.debug("清除互助信息列表缓存成功");
        } catch (Exception e) {
            log.error("清除互助信息列表缓存失败: {}", e.getMessage(), e);
        }
    }
    
    @Override
    public void clearUserCache(Long userId) {
        try {
            String key = HELP_INFO_USER_KEY_PREFIX + userId;
            redisTemplate.delete(key);
            log.debug("清除用户缓存成功, id: {}", userId);
        } catch (Exception e) {
            log.error("清除用户缓存失败: {}", e.getMessage(), e);
        }
    }
    
    @Override
    public void clearAllHelpInfoCache() {
        try {
            // 清除所有互助信息相关缓存
            Set<String> allKeys = new HashSet<>();
            
            Set<String> listKeys = redisTemplate.keys(HELP_INFO_LIST_KEY_PREFIX + "*");
            if (listKeys != null) allKeys.addAll(listKeys);
            
            Set<String> detailKeys = redisTemplate.keys(HELP_INFO_DETAIL_KEY_PREFIX + "*");
            if (detailKeys != null) allKeys.addAll(detailKeys);
            
            Set<String> userKeys = redisTemplate.keys(HELP_INFO_USER_KEY_PREFIX + "*");
            if (userKeys != null) allKeys.addAll(userKeys);
            
            Set<String> adminKeys = redisTemplate.keys(HELP_INFO_ADMIN_KEY_PREFIX + "*");
            if (adminKeys != null) allKeys.addAll(adminKeys);
            
            Set<String> searchKeys = redisTemplate.keys(HELP_INFO_SEARCH_KEY_PREFIX + "*");
            if (searchKeys != null) allKeys.addAll(searchKeys);
            
            if (!allKeys.isEmpty()) {
                redisTemplate.delete(allKeys);
            }
            
            log.info("清除所有互助信息缓存成功, 清除键数量: {}", allKeys.size());
        } catch (Exception e) {
            log.error("清除所有互助信息缓存失败: {}", e.getMessage(), e);
        }
    }
    
    @Override
    public String generateListCacheKey(long page, long size, String type, String status, 
                                      String publisherId, String keyword) {
        StringBuilder keyBuilder = new StringBuilder();
        keyBuilder.append("page:").append(page)
                .append(":size:").append(size);
        
        if (type != null && !type.trim().isEmpty()) {
            keyBuilder.append(":type:").append(type);
        }
        if (status != null && !status.trim().isEmpty()) {
            keyBuilder.append(":status:").append(status);
        }
        if (publisherId != null && !publisherId.trim().isEmpty()) {
            keyBuilder.append(":publisher:").append(publisherId);
        }
        if (keyword != null && !keyword.trim().isEmpty()) {
            keyBuilder.append(":keyword:").append(keyword.hashCode());
        }
        
        return keyBuilder.toString();
    }
    
    @Override
    public String generateSearchCacheKey(String keyword, String type, String status, 
                                        long page, long size) {
        StringBuilder keyBuilder = new StringBuilder();
        keyBuilder.append("search:page:").append(page)
                .append(":size:").append(size);
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            keyBuilder.append(":keyword:").append(keyword.hashCode());
        }
        if (type != null && !type.trim().isEmpty()) {
            keyBuilder.append(":type:").append(type);
        }
        if (status != null && !status.trim().isEmpty()) {
            keyBuilder.append(":status:").append(status);
        }
        
        return keyBuilder.toString();
    }
    
    @Override
    public String generateAdminCacheKey(Integer page, Integer size, String keyword, 
                                       String type, String status) {
        StringBuilder keyBuilder = new StringBuilder();
        keyBuilder.append("admin:page:").append(page)
                .append(":size:").append(size);
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            keyBuilder.append(":keyword:").append(keyword.hashCode());
        }
        if (type != null && !type.trim().isEmpty()) {
            keyBuilder.append(":type:").append(type);
        }
        if (status != null && !status.trim().isEmpty()) {
            keyBuilder.append(":status:").append(status);
        }
        
        return keyBuilder.toString();
    }
    
    @Override
    public void clearAllExpiredCaches() {
        try {
            // 查找所有以指定前缀开头的缓存键
            Set<String> helpInfoDetailKeys = redisTemplate.keys(HELP_INFO_DETAIL_KEY_PREFIX + "*");
            Set<String> helpInfoListKeys = redisTemplate.keys(HELP_INFO_LIST_KEY_PREFIX + "*");
            Set<String> helpInfoUserKeys = redisTemplate.keys(HELP_INFO_USER_KEY_PREFIX + "*");
            Set<String> helpInfoSearchKeys = redisTemplate.keys(HELP_INFO_SEARCH_KEY_PREFIX + "*");
            
            // 合并所有键集合
            Set<String> allKeys = new HashSet<>();
            if (helpInfoDetailKeys != null) allKeys.addAll(helpInfoDetailKeys);
            if (helpInfoListKeys != null) allKeys.addAll(helpInfoListKeys);
            if (helpInfoUserKeys != null) allKeys.addAll(helpInfoUserKeys);
            if (helpInfoSearchKeys != null) allKeys.addAll(helpInfoSearchKeys);
            
            // 批量删除所有找到的键
            if (!allKeys.isEmpty()) {
                redisTemplate.delete(allKeys);
                log.info("清理过期缓存完成，共清理 {} 个缓存项", allKeys.size());
            } else {
                log.info("没有找到需要清理的过期缓存");
            }
            
        } catch (Exception e) {
            log.error("清理过期缓存失败: {}", e.getMessage(), e);
        }
    }
}
