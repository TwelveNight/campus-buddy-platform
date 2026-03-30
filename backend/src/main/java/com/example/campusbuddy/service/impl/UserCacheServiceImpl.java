package com.example.campusbuddy.service.impl;

import com.example.campusbuddy.entity.User;
import com.example.campusbuddy.mapper.UserMapper;
import com.example.campusbuddy.service.UserCacheService;
import com.example.campusbuddy.vo.UserVO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;

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
    private static final String USER_CACHE_PREFIX    = "campus:user:";
    private static final String USER_VO_CACHE_PREFIX = "campus:user:vo:";
    private static final String USER_USERNAME_PREFIX = "campus:user:username:";
    private static final String USER_CREDIT_PREFIX   = "campus:user:credit:";

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
        // 缓存未命中，从 User 回源构建 UserVO
        try {
            User user = userMapper.selectById(userId);
            if (user != null) {
                UserVO userVO = new UserVO();
                org.springframework.beans.BeanUtils.copyProperties(user, userVO);
                cacheUserVO(userVO);
                return userVO;
            }
        } catch (Exception e) {
            log.error("从数据库回源用户VO信息失败: userId={}", userId, e);
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
    public UserVO getUserVOFromCache(Long userId) {
        return getCachedUserVO(userId);
    }

    @Override
    public void evictUserCache(Long userId) {
        if (userId == null) {
            return;
        }
        try {
            redisTemplate.delete(USER_CACHE_PREFIX + userId);
            redisTemplate.delete(USER_VO_CACHE_PREFIX + userId);
            redisTemplate.delete(USER_CREDIT_PREFIX + userId);
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
            redisTemplate.delete(USER_USERNAME_PREFIX + username);
            log.info("已清除用户名缓存: username={}", username);
        } catch (Exception e) {
            log.error("清除用户名缓存失败: username={}", username, e);
        }
    }

    @Override
    public void cacheCreditScore(Long userId, Integer creditScore, long expireSeconds) {
        if (userId == null || creditScore == null) {
            return;
        }
        try {
            redisTemplate.opsForValue().set(USER_CREDIT_PREFIX + userId, creditScore, expireSeconds, TimeUnit.SECONDS);
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
            Object score = redisTemplate.opsForValue().get(USER_CREDIT_PREFIX + userId);
            if (score != null) {
                return (Integer) score;
            }
        } catch (Exception e) {
            log.error("读取缓存用户信用积分失败: userId={}", userId, e);
        }
        return null;
    }

    @Override
    public void evictCreditScoreCache(Long userId) {
        if (userId == null) {
            return;
        }
        try {
            redisTemplate.delete(USER_CREDIT_PREFIX + userId);
        } catch (Exception e) {
            log.error("清除用户信用积分缓存失败: userId={}", userId, e);
        }
    }

    @Override
    public void batchCacheCreditScores(Map<Long, Integer> creditScores, long expireSeconds) {
        if (creditScores == null || creditScores.isEmpty()) {
            return;
        }
        try {
            for (Map.Entry<Long, Integer> entry : creditScores.entrySet()) {
                redisTemplate.opsForValue().set(USER_CREDIT_PREFIX + entry.getKey(), entry.getValue(), expireSeconds, TimeUnit.SECONDS);
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
            List<String> keys = new ArrayList<>();
            ScanOptions options = ScanOptions.scanOptions().match("campus:*").count(200).build();
            try (Cursor<Object> cursor = redisTemplate.scan(options)) {
                cursor.forEachRemaining(k -> keys.add(k.toString()));
            }
            if (!keys.isEmpty()) {
                redisTemplate.delete(keys);
                log.info("缓存清理完成，共删除{}个key", keys.size());
            } else {
                log.info("没有需要清理的缓存");
            }
        } catch (Exception e) {
            log.error("清空缓存失败", e);
        }
    }
}
