package com.example.campusbuddy.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.campusbuddy.entity.GroupPost;
import com.example.campusbuddy.entity.User;
import com.example.campusbuddy.service.GroupPostCacheService;
import com.example.campusbuddy.vo.GroupPostVO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
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
import java.util.stream.Collectors;

/**
 * 帖子缓存服务实现类
 */
@Service
@Slf4j
public class GroupPostCacheServiceImpl implements GroupPostCacheService {
    
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    // Redis key 前缀
    private static final String GROUP_POSTS_KEY_PREFIX = "campus:group:posts:";
    private static final String POST_DETAIL_KEY_PREFIX = "campus:post:detail:";
    private static final String POST_USER_KEY_PREFIX = "campus:post:user:";
    private static final String HOT_POSTS_KEY = "campus:post:hot";
    
    // 缓存过期时间（秒）
    private static final long POSTS_CACHE_EXPIRE = 1800; // 30分钟
    private static final long POST_DETAIL_CACHE_EXPIRE = 3600; // 1小时
    private static final long USER_CACHE_EXPIRE = 3600; // 1小时
    private static final long HOT_POSTS_CACHE_EXPIRE = 1800; // 30分钟

    @Override
    public void cacheGroupPosts(Long groupId, Integer pageNum, Integer pageSize, IPage<GroupPostVO> page, long expireSeconds) {
        if (groupId == null || page == null) {
            return;
        }
        try {
            String key = GROUP_POSTS_KEY_PREFIX + groupId + ":" + pageNum + ":" + pageSize;
            String pageJson = objectMapper.writeValueAsString(page);
            redisTemplate.opsForValue().set(key, pageJson, expireSeconds, TimeUnit.SECONDS);
            log.debug("缓存小组帖子列表: groupId={}, pageNum={}, pageSize={}, total={}", 
                     groupId, pageNum, pageSize, page.getTotal());
        } catch (JsonProcessingException e) {
            log.error("缓存小组帖子列表失败: groupId={}, pageNum={}, pageSize={}", 
                     groupId, pageNum, pageSize, e);
        }
    }

    @Override
    public IPage<GroupPostVO> getCachedGroupPosts(Long groupId, Integer pageNum, Integer pageSize) {
        if (groupId == null) {
            return null;
        }
        try {
            String key = GROUP_POSTS_KEY_PREFIX + groupId + ":" + pageNum + ":" + pageSize;
            String pageJson = redisTemplate.opsForValue().get(key);
            if (pageJson != null) {
                IPage<GroupPostVO> page = objectMapper.readValue(pageJson, 
                        new TypeReference<Page<GroupPostVO>>() {});
                log.debug("从缓存获取小组帖子列表: groupId={}, pageNum={}, pageSize={}, total={}", 
                         groupId, pageNum, pageSize, page.getTotal());
                return page;
            }
        } catch (Exception e) {
            log.error("从缓存获取小组帖子列表失败: groupId={}, pageNum={}, pageSize={}", 
                     groupId, pageNum, pageSize, e);
        }
        return null;
    }

    @Override
    public void cachePostDetail(Long postId, GroupPostVO postVO, long expireSeconds) {
        if (postId == null || postVO == null) {
            return;
        }
        try {
            String key = POST_DETAIL_KEY_PREFIX + postId;
            String postJson = objectMapper.writeValueAsString(postVO);
            redisTemplate.opsForValue().set(key, postJson, expireSeconds, TimeUnit.SECONDS);
            log.debug("缓存帖子详情: postId={}", postId);
        } catch (JsonProcessingException e) {
            log.error("缓存帖子详情失败: postId={}", postId, e);
        }
    }

    @Override
    public GroupPostVO getCachedPostDetail(Long postId) {
        if (postId == null) {
            return null;
        }
        try {
            String key = POST_DETAIL_KEY_PREFIX + postId;
            String postJson = redisTemplate.opsForValue().get(key);
            if (postJson != null) {
                GroupPostVO postVO = objectMapper.readValue(postJson, GroupPostVO.class);
                log.debug("从缓存获取帖子详情: postId={}", postId);
                return postVO;
            }
        } catch (Exception e) {
            log.error("从缓存获取帖子详情失败: postId={}", postId, e);
        }
        return null;
    }

    @Override
    public void batchCacheUsers(Map<Long, User> userMap, long expireSeconds) {
        if (userMap == null || userMap.isEmpty()) {
            return;
        }
        try {
            for (Map.Entry<Long, User> entry : userMap.entrySet()) {
                Long userId = entry.getKey();
                User user = entry.getValue();
                if (userId != null && user != null) {
                    String key = POST_USER_KEY_PREFIX + userId;
                    String userJson = objectMapper.writeValueAsString(user);
                    redisTemplate.opsForValue().set(key, userJson, expireSeconds, TimeUnit.SECONDS);
                }
            }
            log.debug("批量缓存用户信息: count={}", userMap.size());
        } catch (Exception e) {
            log.error("批量缓存用户信息失败", e);
        }
    }

    @Override
    public User getCachedUser(Long userId) {
        if (userId == null) {
            return null;
        }
        try {
            String key = POST_USER_KEY_PREFIX + userId;
            String userJson = redisTemplate.opsForValue().get(key);
            if (userJson != null) {
                User user = objectMapper.readValue(userJson, User.class);
                log.debug("从缓存获取用户信息: userId={}", userId);
                return user;
            }
        } catch (Exception e) {
            log.error("从缓存获取用户信息失败: userId={}", userId, e);
        }
        return null;
    }

    @Override
    public Map<Long, User> getBatchCachedUsers(List<Long> userIds) {
        Map<Long, User> result = new HashMap<>();
        if (userIds == null || userIds.isEmpty()) {
            return result;
        }
        
        for (Long userId : userIds) {
            User user = getCachedUser(userId);
            if (user != null) {
                result.put(userId, user);
            }
        }
        
        log.debug("批量获取缓存用户信息: requestCount={}, foundCount={}", 
                 userIds.size(), result.size());
        return result;
    }

    @Override
    public void cacheHotPosts(List<GroupPostVO> posts, long expireSeconds) {
        if (posts == null) {
            return;
        }
        try {
            String postsJson = objectMapper.writeValueAsString(posts);
            redisTemplate.opsForValue().set(HOT_POSTS_KEY, postsJson, expireSeconds, TimeUnit.SECONDS);
            log.debug("缓存热门帖子列表: count={}", posts.size());
        } catch (JsonProcessingException e) {
            log.error("缓存热门帖子列表失败", e);
        }
    }

    @Override
    public List<GroupPostVO> getCachedHotPosts() {
        try {
            String postsJson = redisTemplate.opsForValue().get(HOT_POSTS_KEY);
            if (postsJson != null) {
                List<GroupPostVO> posts = objectMapper.readValue(postsJson, 
                        new TypeReference<List<GroupPostVO>>() {});
                log.debug("从缓存获取热门帖子列表: count={}", posts.size());
                return posts;
            }
        } catch (Exception e) {
            log.error("从缓存获取热门帖子列表失败", e);
        }
        return null;
    }

    @Override
    public void evictGroupPostsCache(Long groupId) {
        try {
            if (groupId != null) {
                // 清除特定小组的帖子列表缓存
                String pattern = GROUP_POSTS_KEY_PREFIX + groupId + ":*";
                clearByPattern(pattern);
            } else {
                // 清除所有小组的帖子列表缓存
                String pattern = GROUP_POSTS_KEY_PREFIX + "*";
                clearByPattern(pattern);
            }
            log.debug("清除小组帖子列表缓存: groupId={}", groupId != null ? groupId : "全部");
        } catch (Exception e) {
            log.error("清除小组帖子列表缓存失败: groupId={}", groupId != null ? groupId : "全部", e);
        }
    }

    @Override
    public void evictPostDetailCache(Long postId) {
        if (postId == null) {
            return;
        }
        try {
            String key = POST_DETAIL_KEY_PREFIX + postId;
            redisTemplate.delete(key);
            log.debug("清除帖子详情缓存: postId={}", postId);
        } catch (Exception e) {
            log.error("清除帖子详情缓存失败: postId={}", postId, e);
        }
    }

    @Override
    public void evictUserCache(Long userId) {
        if (userId == null) {
            return;
        }
        try {
            String key = POST_USER_KEY_PREFIX + userId;
            redisTemplate.delete(key);
            log.debug("清除用户缓存: userId={}", userId);
        } catch (Exception e) {
            log.error("清除用户缓存失败: userId={}", userId, e);
        }
    }

    @Override
    public void evictHotPostsCache() {
        try {
            redisTemplate.delete(HOT_POSTS_KEY);
            log.debug("清除热门帖子缓存");
        } catch (Exception e) {
            log.error("清除热门帖子缓存失败", e);
        }
    }

    @Override
    public void evictAllPostsCache() {
        try {
            // 清除所有帖子列表缓存
            clearByPattern(GROUP_POSTS_KEY_PREFIX + "*");
            // 清除所有帖子详情缓存
            clearByPattern(POST_DETAIL_KEY_PREFIX + "*");
            // 清除热门帖子缓存
            redisTemplate.delete(HOT_POSTS_KEY);
            log.info("清除所有帖子相关缓存");
        } catch (Exception e) {
            log.error("清除所有帖子相关缓存失败", e);
        }
    }
    
    /**
     * 按模式清除缓存
     */
    private void clearByPattern(String pattern) {
        try {
            if (pattern == null || pattern.isEmpty()) {
                return;
            }
            
            // 获取符合模式的所有key
            var keys = redisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                // 批量删除
                redisTemplate.delete(keys);
                log.debug("按模式清除缓存: pattern={}, count={}", pattern, keys.size());
            }
        } catch (Exception e) {
            log.error("按模式清除缓存失败: pattern={}", pattern, e);
        }
    }
}
