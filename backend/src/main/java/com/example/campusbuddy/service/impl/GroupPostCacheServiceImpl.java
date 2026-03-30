package com.example.campusbuddy.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
import java.util.List;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import java.util.concurrent.TimeUnit;
// Set import removed — clearByPattern now uses SCAN cursor

@Service
@Slf4j
public class GroupPostCacheServiceImpl implements GroupPostCacheService {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String GROUP_POSTS_KEY_PREFIX = "campus:group:posts:";
    private static final String POST_DETAIL_KEY_PREFIX = "campus:post:detail:";
    @Override
    public void cacheGroupPosts(Long groupId, Integer pageNum, Integer pageSize, IPage<GroupPostVO> page,
            long expireSeconds) {
        if (groupId == null || page == null) {
            return;
        }
        try {
            String key = GROUP_POSTS_KEY_PREFIX + groupId + ":" + pageNum + ":" + pageSize;
            String pageJson = objectMapper.writeValueAsString(page);
            redisTemplate.opsForValue().set(key, pageJson, expireSeconds, TimeUnit.SECONDS);
        } catch (JsonProcessingException e) {
            log.error("缓存小组帖子列表失败: groupId={}, pageNum={}, pageSize={}", groupId, pageNum, pageSize, e);
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
                return objectMapper.readValue(pageJson, new TypeReference<Page<GroupPostVO>>() {});
            }
        } catch (Exception e) {
            log.error("从缓存获取小组帖子列表失败: groupId={}", groupId, e);
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
                return objectMapper.readValue(postJson, GroupPostVO.class);
            }
        } catch (Exception e) {
            log.error("从缓存获取帖子详情失败: postId={}", postId, e);
        }
        return null;
    }

    @Override
    public void evictGroupPostsCache(Long groupId) {
        try {
            String pattern = groupId != null
                    ? GROUP_POSTS_KEY_PREFIX + groupId + ":*"
                    : GROUP_POSTS_KEY_PREFIX + "*";
            clearByPattern(pattern);
        } catch (Exception e) {
            log.error("清除小组帖子列表缓存失败: groupId={}", groupId, e);
        }
    }

    @Override
    public void evictPostDetailCache(Long postId) {
        if (postId == null) {
            return;
        }
        try {
            redisTemplate.delete(POST_DETAIL_KEY_PREFIX + postId);
        } catch (Exception e) {
            log.error("清除帖子详情缓存失败: postId={}", postId, e);
        }
    }

    @Override
    public void evictAllPostsCache() {
        try {
            clearByPattern(GROUP_POSTS_KEY_PREFIX + "*");
            clearByPattern(POST_DETAIL_KEY_PREFIX + "*");
        } catch (Exception e) {
            log.error("清除所有帖子相关缓存失败", e);
        }
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
}
