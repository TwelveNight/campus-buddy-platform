package com.example.campusbuddy.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.campusbuddy.entity.User;
import com.example.campusbuddy.mapper.UserMapper;
import com.example.campusbuddy.service.UserCacheService;
import com.example.campusbuddy.vo.UserVO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class UserCacheServiceImpl implements UserCacheService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserMapper userMapper;

    // 缓存key前缀
    private static final String USER_CACHE_PREFIX = "campus:user:";
    private static final String USER_VO_CACHE_PREFIX = "campus:user:vo:";
    private static final String USER_TOKEN_PREFIX = "campus:user:token:";
    private static final String USER_USERNAME_PREFIX = "campus:user:username:";
    private static final String USER_SEARCH_PREFIX = "campus:user:search:";
    private static final String USER_LIST_PREFIX = "campus:user:list:";
    private static final String USER_CREDIT_PREFIX = "campus:user:credit:";

    // 过期时间（秒）
    private static final long USER_CACHE_EXPIRE = 3600; // 1小时

    @Override
    public void cacheUser(User user) {
        if (user == null || user.getUserId() == null) {
            return;
        }
        
        try {
            String key = USER_CACHE_PREFIX + user.getUserId();
            String userJson = objectMapper.writeValueAsString(user);
            redisTemplate.opsForValue().set(key, userJson, USER_CACHE_EXPIRE, TimeUnit.SECONDS);
            
            // 同时按用户名缓存
            if (user.getUsername() != null) {
                String usernameKey = USER_USERNAME_PREFIX + user.getUsername();
                redisTemplate.opsForValue().set(usernameKey, userJson, USER_CACHE_EXPIRE, TimeUnit.SECONDS);
            }
        } catch (JsonProcessingException e) {
            log.error("缓存用户信息失败: userId={}", user.getUserId(), e);
        }
    }

    @Override
    public User getCachedUser(Long userId) {
        if (userId == null) {
            return null;
        }
        
        try {
            String key = USER_CACHE_PREFIX + userId;
            String userJson = (String) redisTemplate.opsForValue().get(key);
            if (userJson != null) {
                return objectMapper.readValue(userJson, User.class);
            }
        } catch (Exception e) {
            log.error("读取缓存用户信息失败: userId={}", userId, e);
        }
        // 缓存未命中，从数据库回源并重新缓存
        try {
            User user = userMapper.selectById(userId);
            if (user != null) {
                cacheUser(user);
            }
            return user;
        } catch (Exception e) {
            log.error("从数据库回源用户信息失败: userId={}", userId, e);
        }
        return null;
    }

    @Override
    public void cacheUserVO(UserVO userVO) {
        if (userVO == null || userVO.getUserId() == null) {
            return;
        }
        
        try {
            String key = USER_VO_CACHE_PREFIX + userVO.getUserId();
            String userVOJson = objectMapper.writeValueAsString(userVO);
            redisTemplate.opsForValue().set(key, userVOJson, USER_CACHE_EXPIRE, TimeUnit.SECONDS);
        } catch (JsonProcessingException e) {
            log.error("缓存用户VO信息失败: userId={}", userVO.getUserId(), e);
        }
    }

    @Override
    public UserVO getCachedUserVO(Long userId) {
        if (userId == null) {
            return null;
        }
        
        try {
            String key = USER_VO_CACHE_PREFIX + userId;
            String userVOJson = (String) redisTemplate.opsForValue().get(key);
            if (userVOJson != null) {
                return objectMapper.readValue(userVOJson, UserVO.class);
            }
        } catch (Exception e) {
            log.error("读取缓存用户VO信息失败: userId={}", userId, e);
        }
        return null;
    }

    @Override
    public User getUserByUsernameFromCache(String username) {
        if (username == null || username.trim().isEmpty()) {
            return null;
        }
        
        try {
            String key = USER_USERNAME_PREFIX + username;
            String userJson = (String) redisTemplate.opsForValue().get(key);
            if (userJson != null) {
                return objectMapper.readValue(userJson, User.class);
            }
        } catch (Exception e) {
            log.error("从缓存按用户名获取用户信息失败: username={}", username, e);
        }
        return null;
    }

    @Override
    public void evictUserCache(Long userId) {
        if (userId == null) {
            return;
        }
        
        try {
            String userKey = USER_CACHE_PREFIX + userId;
            String userVOKey = USER_VO_CACHE_PREFIX + userId;
            String creditKey = USER_CREDIT_PREFIX + userId;
            
            redisTemplate.delete(userKey);
            redisTemplate.delete(userVOKey);
            redisTemplate.delete(creditKey);
            
            log.info("已清除用户缓存: userId={}", userId);
        } catch (Exception e) {
            log.error("清除用户缓存失败: userId={}", userId, e);
        }
    }

    @Override
    public void evictUserCacheByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return;
        }
        
        try {
            String key = USER_USERNAME_PREFIX + username;
            redisTemplate.delete(key);
            log.info("已清除用户名缓存: username={}", username);
        } catch (Exception e) {
            log.error("清除用户名缓存失败: username={}", username, e);
        }
    }

    @Override
    public void evictUserToken(Long userId) {
        if (userId == null) {
            return;
        }
        
        try {
            // 清除该用户的所有token
            String pattern = USER_TOKEN_PREFIX + "*:" + userId;
            Set<String> keys = redisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                log.info("已清除用户Token缓存: userId={}, count={}", userId, keys.size());
            }
        } catch (Exception e) {
            log.error("清除用户Token缓存失败: userId={}", userId, e);
        }
    }

    @Override
    public void cacheSearchResult(String keyword, Integer page, Integer size, Page<UserVO> result, long expireSeconds) {
        if (keyword == null || page == null || size == null || result == null) {
            return;
        }
        
        try {
            String key = USER_SEARCH_PREFIX + keyword + ":" + page + ":" + size;
            String resultJson = objectMapper.writeValueAsString(result);
            redisTemplate.opsForValue().set(key, resultJson, expireSeconds, TimeUnit.SECONDS);
        } catch (JsonProcessingException e) {
            log.error("缓存搜索结果失败: keyword={}, page={}, size={}", keyword, page, size, e);
        }
    }

    @Override
    public Page<UserVO> getSearchResultFromCache(String keyword, Integer page, Integer size) {
        if (keyword == null || page == null || size == null) {
            return null;
        }
        
        try {
            String key = USER_SEARCH_PREFIX + keyword + ":" + page + ":" + size;
            String resultJson = (String) redisTemplate.opsForValue().get(key);
            if (resultJson != null) {
                return objectMapper.readValue(resultJson, new TypeReference<Page<UserVO>>() {});
            }
        } catch (Exception e) {
            log.error("读取缓存搜索结果失败: keyword={}, page={}, size={}", keyword, page, size, e);
        }
        return null;
    }

    @Override
    public void evictSearchCache() {
        try {
            Set<String> keys = redisTemplate.keys(USER_SEARCH_PREFIX + "*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                log.info("已清除用户搜索缓存，共{}个", keys.size());
            }
        } catch (Exception e) {
            log.error("清除用户搜索缓存失败", e);
        }
    }

    @Override
    public void cacheCreditScore(Long userId, Integer creditScore, long expireSeconds) {
        if (userId == null || creditScore == null) {
            return;
        }
        
        try {
            String key = USER_CREDIT_PREFIX + userId;
            redisTemplate.opsForValue().set(key, creditScore, expireSeconds, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("缓存用户信用积分失败: userId={}, score={}", userId, creditScore, e);
        }
    }

    @Override
    public Integer getCachedCreditScore(Long userId) {
        if (userId == null) {
            return null;
        }
        
        try {
            String key = USER_CREDIT_PREFIX + userId;
            Object score = redisTemplate.opsForValue().get(key);
            if (score != null) {
                return (Integer) score;
            }
        } catch (Exception e) {
            log.error("读取缓存用户信用积分失败: userId={}", userId, e);
        }
        return null;
    }

    @Override
    public void batchCacheCreditScores(Map<Long, Integer> creditScores, long expireSeconds) {
        if (creditScores == null || creditScores.isEmpty()) {
            return;
        }
        
        try {
            for (Map.Entry<Long, Integer> entry : creditScores.entrySet()) {
                String key = USER_CREDIT_PREFIX + entry.getKey();
                redisTemplate.opsForValue().set(key, entry.getValue(), expireSeconds, TimeUnit.SECONDS);
            }
            log.info("批量缓存用户信用积分成功，共{}个", creditScores.size());
        } catch (Exception e) {
            log.error("批量缓存用户信用积分失败", e);
        }
    }

    @Override
    public Map<Long, Integer> getBatchCachedCreditScores(List<Long> userIds) {
        Map<Long, Integer> result = new HashMap<>();
        for (Long userId : userIds) {
            Integer creditScore = getCachedCreditScore(userId);
            if (creditScore != null) {
                result.put(userId, creditScore);
            }
        }
        return result;
    }
    
    @Override
    public void flushAllCache() {
        try {
            log.info("开始清空所有缓存...");
            
            // 方法1：清理所有campus:前缀的key
            try {
                Set<String> keys = redisTemplate.keys("campus:*");
                if (keys != null && !keys.isEmpty()) {
                    redisTemplate.delete(keys);
                    log.info("通过keys模式清理缓存，删除了{}个key", keys.size());
                    return;
                }
            } catch (Exception e) {
                log.warn("keys模式清理失败，尝试分别清理各前缀: {}", e.getMessage());
            }
            
            // 方法2：分别清理各个前缀的缓存
            String[] prefixes = {
                USER_CACHE_PREFIX,
                USER_VO_CACHE_PREFIX,
                USER_TOKEN_PREFIX,
                USER_USERNAME_PREFIX,
                USER_SEARCH_PREFIX,
                USER_LIST_PREFIX,
                USER_CREDIT_PREFIX
            };
            
            int totalDeleted = 0;
            for (String prefix : prefixes) {
                try {
                    Set<String> keys = redisTemplate.keys(prefix + "*");
                    if (keys != null && !keys.isEmpty()) {
                        redisTemplate.delete(keys);
                        totalDeleted += keys.size();
                        log.info("清理前缀 {} 的缓存，删除{}个key", prefix, keys.size());
                    }
                } catch (Exception e) {
                    log.warn("清理前缀 {} 失败: {}", prefix, e.getMessage());
                }
            }
            
            log.info("缓存清理完成，共删除{}个key", totalDeleted);
            
        } catch (Exception e) {
            log.error("清空缓存失败", e);
        }
    }

    @Override
    public void cacheUserToken(Long userId, String token, long expireSeconds) {
        if (userId == null || token == null) {
            return;
        }
        
        try {
            String key = USER_TOKEN_PREFIX + userId;
            redisTemplate.opsForValue().set(key, token, expireSeconds, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("缓存用户Token失败: userId={}", userId, e);
        }
    }

    @Override
    public String getCachedUserToken(Long userId) {
        if (userId == null) {
            return null;
        }
        
        try {
            String key = USER_TOKEN_PREFIX + userId;
            return (String) redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.error("获取缓存用户Token失败: userId={}", userId, e);
            return null;
        }
    }

    @Override
    public String getUserTokenFromCache(Long userId) {
        return getCachedUserToken(userId);
    }

    @Override
    public UserVO getUserVOFromCache(Long userId) {
        return getCachedUserVO(userId);
    }

    @Override
    public void evictCreditScoreCache(Long userId) {
        if (userId == null) {
            return;
        }
        
        try {
            String key = USER_CREDIT_PREFIX + userId;
            redisTemplate.delete(key);
        } catch (Exception e) {
            log.error("清除用户信用积分缓存失败: userId={}", userId, e);
        }
    }
}
