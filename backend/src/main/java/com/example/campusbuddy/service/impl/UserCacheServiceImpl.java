package com.example.campusbuddy.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.campusbuddy.entity.User;
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
import java.util.concurrent.TimeUnit;

/**
 * 用户缓存服务实现类
 */
@Service
@Slf4j
public class UserCacheServiceImpl implements UserCacheService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // ================== Redis key 前缀说明 ==================
    // campus:user:{userId} —— 缓存用户基础信息（User 实体）
    // campus:user:vo:{userId} —— 缓存用户视图对象（UserVO，前端展示用）
    // campus:user:token:{userId} —— 缓存用户登录 token
    // campus:user:search:{...} —— 缓存用户搜索结果（分页/关键词）
    // campus:username:{username} —— 缓存用户名到用户信息的映射
    // campus:user:credit:{userId} —— 缓存用户信用分/积分
    // =======================================================

    // Redis key 前缀
    private static final String USER_KEY_PREFIX = "campus:user:";
    private static final String USER_VO_KEY_PREFIX = "campus:user:vo:";
    private static final String USER_TOKEN_KEY_PREFIX = "campus:user:token:";
    private static final String USER_SEARCH_KEY_PREFIX = "campus:user:search:";
    private static final String USERNAME_KEY_PREFIX = "campus:username:";
    private static final String USER_CREDIT_KEY_PREFIX = "campus:user:credit:";

    // 缓存过期时间（秒）
    private static final long USER_CACHE_EXPIRE = 3600; // 1小时

    @Override
    public void cacheUser(User user) {
        if (user == null || user.getUserId() == null) {
            return;
        }
        try {
            String key = USER_KEY_PREFIX + user.getUserId();
            String userJson = objectMapper.writeValueAsString(user);
            redisTemplate.opsForValue().set(key, userJson, USER_CACHE_EXPIRE, TimeUnit.SECONDS);

            // 同时缓存用户名到用户信息的映射
            if (user.getUsername() != null) {
                String usernameKey = USERNAME_KEY_PREFIX + user.getUsername();
                redisTemplate.opsForValue().set(usernameKey, userJson, USER_CACHE_EXPIRE, TimeUnit.SECONDS);
            }

            log.debug("缓存用户信息: userId={}", user.getUserId());
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
            String key = USER_KEY_PREFIX + userId;
            String userJson = (String) redisTemplate.opsForValue().get(key);
            if (userJson != null) {
                return objectMapper.readValue(userJson, User.class);
            }
        } catch (JsonProcessingException e) {
            log.error("读取缓存用户信息失败: userId={}", userId, e);
        }
        return null;
    }

    @Override
    public void cacheUserVO(UserVO userVO) {
        if (userVO == null || userVO.getUserId() == null) {
            return;
        }
        try {
            String key = USER_VO_KEY_PREFIX + userVO.getUserId();
            String userVOJson = objectMapper.writeValueAsString(userVO);
            redisTemplate.opsForValue().set(key, userVOJson, USER_CACHE_EXPIRE, TimeUnit.SECONDS);
            log.debug("缓存用户VO信息: userId={}", userVO.getUserId());
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
            String key = USER_VO_KEY_PREFIX + userId;
            String userVOJson = (String) redisTemplate.opsForValue().get(key);
            if (userVOJson != null) {
                return objectMapper.readValue(userVOJson, UserVO.class);
            }
        } catch (JsonProcessingException e) {
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
            String key = USERNAME_KEY_PREFIX + username;
            String userJson = (String) redisTemplate.opsForValue().get(key);
            if (userJson != null) {
                return objectMapper.readValue(userJson, User.class);
            }
        } catch (JsonProcessingException e) {
            log.error("读取缓存用户信息失败: username={}", username, e);
        }
        return null;
    }

    @Override
    public void cacheUserToken(Long userId, String token, long expireSeconds) {
        if (userId == null || token == null) {
            return;
        }
        String key = USER_TOKEN_KEY_PREFIX + userId;
        redisTemplate.opsForValue().set(key, token, expireSeconds, TimeUnit.SECONDS);
        log.debug("缓存用户token: userId={}", userId);
    }

    @Override
    public String getCachedUserToken(Long userId) {
        if (userId == null) {
            return null;
        }
        String key = USER_TOKEN_KEY_PREFIX + userId;
        return (String) redisTemplate.opsForValue().get(key);
    }

    @Override
    public void evictUserCache(Long userId) {
        if (userId == null) {
            return;
        }
        String userKey = USER_KEY_PREFIX + userId;
        String userVOKey = USER_VO_KEY_PREFIX + userId;
        redisTemplate.delete(userKey);
        redisTemplate.delete(userVOKey);
        log.debug("清除用户缓存: userId={}", userId);
    }

    @Override
    public void evictUserToken(Long userId) {
        if (userId == null) {
            return;
        }
        String key = USER_TOKEN_KEY_PREFIX + userId;
        redisTemplate.delete(key);
        log.debug("清除用户token缓存: userId={}", userId);
    }

    @Override
    public void cacheSearchResult(String keyword, Integer page, Integer size, Page<UserVO> result, long expireSeconds) {
        if (keyword == null || page == null || size == null || result == null) {
            return;
        }
        try {
            String key = USER_SEARCH_KEY_PREFIX + keyword + ":" + page + ":" + size;
            String resultJson = objectMapper.writeValueAsString(result);
            redisTemplate.opsForValue().set(key, resultJson, expireSeconds, TimeUnit.SECONDS);
            log.debug("缓存搜索结果: keyword={}, page={}, size={}", keyword, page, size);
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
            String key = USER_SEARCH_KEY_PREFIX + keyword + ":" + page + ":" + size;
            String resultJson = (String) redisTemplate.opsForValue().get(key);
            if (resultJson != null) {
                return objectMapper.readValue(resultJson, new TypeReference<Page<UserVO>>() {
                });
            }
        } catch (JsonProcessingException e) {
            log.error("读取缓存搜索结果失败: keyword={}, page={}, size={}", keyword, page, size, e);
        }
        return null;
    }

    @Override
    public void cacheSearchResult(String keyword, Integer page, Integer size, List<UserVO> userList,
            long expireSeconds) {
        if (keyword == null || page == null || size == null || userList == null) {
            return;
        }
        try {
            String key = USER_SEARCH_KEY_PREFIX + keyword + ":" + page + ":" + size;
            String userListJson = objectMapper.writeValueAsString(userList);
            redisTemplate.opsForValue().set(key, userListJson, expireSeconds, TimeUnit.SECONDS);
            log.debug("缓存分页搜索结果: keyword={}, page={}, size={}", keyword, page, size);
        } catch (JsonProcessingException e) {
            log.error("缓存分页搜索结果失败: keyword={}, page={}, size={}", keyword, page, size, e);
        }
    }

    @Override
    public List<UserVO> getCachedSearchResult(String keyword, Integer page, Integer size) {
        if (keyword == null || page == null || size == null) {
            return null;
        }
        try {
            String key = USER_SEARCH_KEY_PREFIX + keyword + ":" + page + ":" + size;
            String userListJson = (String) redisTemplate.opsForValue().get(key);
            if (userListJson != null) {
                return objectMapper.readValue(userListJson, new TypeReference<List<UserVO>>() {
                });
            }
        } catch (JsonProcessingException e) {
            log.error("读取缓存分页搜索结果失败: keyword={}, page={}, size={}", keyword, page, size, e);
        }
        return null;
    }

    @Override
    public void evictSearchCache() {
        try {
            redisTemplate.delete(redisTemplate.keys(USER_SEARCH_KEY_PREFIX + "*"));
            log.debug("清除所有用户搜索缓存");
        } catch (Exception e) {
            log.error("清除用户搜索缓存失败", e);
        }
    }

    @Override
    public UserVO getUserVOFromCache(Long userId) {
        return getCachedUserVO(userId);
    }

    @Override
    public String getUserTokenFromCache(Long userId) {
        return getCachedUserToken(userId);
    }

    @Override
    public void cacheCreditScore(Long userId, Integer creditScore, long expireSeconds) {
        if (userId == null || creditScore == null) {
            return;
        }
        try {
            String key = USER_CREDIT_KEY_PREFIX + userId;
            // 将Integer转换为String再存储，以适应StringRedisSerializer
            redisTemplate.opsForValue().set(key, creditScore.toString(), expireSeconds, TimeUnit.SECONDS);
            log.debug("缓存用户信用积分: userId={}, creditScore={}, expireSeconds={}", userId, creditScore, expireSeconds);
        } catch (Exception e) {
            log.error("缓存用户信用积分失败: userId={}, creditScore={}", userId, creditScore, e);
        }
    }

    @Override
    public Integer getCachedCreditScore(Long userId) {
        if (userId == null) {
            return null;
        }
        try {
            String key = USER_CREDIT_KEY_PREFIX + userId;
            Object value = redisTemplate.opsForValue().get(key);
            if (value != null) {
                // 从String转回Integer
                Integer creditScore = value instanceof String ? Integer.parseInt((String) value) : (Integer) value;
                log.debug("从缓存获取用户信用积分: userId={}, creditScore={}", userId, creditScore);
                return creditScore;
            }
        } catch (Exception e) {
            log.error("从缓存获取用户信用积分失败: userId={}", userId, e);
        }
        return null;
    }

    @Override
    public void evictCreditScoreCache(Long userId) {
        if (userId == null) {
            return;
        }
        try {
            String key = USER_CREDIT_KEY_PREFIX + userId;
            redisTemplate.delete(key);
            log.debug("清除用户信用积分缓存: userId={}", userId);
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
                cacheCreditScore(entry.getKey(), entry.getValue(), expireSeconds);
            }
            log.debug("批量缓存用户信用积分: count={}", creditScores.size());
        } catch (Exception e) {
            log.error("批量缓存用户信用积分失败", e);
        }
    }

    @Override
    public Map<Long, Integer> getBatchCachedCreditScores(List<Long> userIds) {
        Map<Long, Integer> result = new HashMap<>();
        if (userIds == null || userIds.isEmpty()) {
            return result;
        }

        try {
            for (Long userId : userIds) {
                Integer creditScore = getCachedCreditScore(userId);
                if (creditScore != null) {
                    result.put(userId, creditScore);
                }
            }
            log.debug("批量获取用户信用积分缓存: requestCount={}, foundCount={}", userIds.size(), result.size());
        } catch (Exception e) {
            log.error("批量获取用户信用积分缓存失败", e);
        }

        return result;
    }
}
