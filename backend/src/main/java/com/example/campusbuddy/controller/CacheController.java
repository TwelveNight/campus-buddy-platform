package com.example.campusbuddy.controller;

import com.example.campusbuddy.common.R;
import com.example.campusbuddy.service.GroupPostCacheService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 缓存管理控制器
 * 用于监控和管理Redis缓存
 */
@RestController
@RequestMapping("/api/cache")
@Slf4j
public class CacheController {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    @Autowired
    private GroupPostCacheService postCacheService;

    /**
     * 获取缓存统计信息
     */
    @GetMapping("/stats")
    public R<Map<String, Object>> getCacheStats() {
        try {
            Map<String, Object> stats = new HashMap<>();
            
            // 获取不同类型缓存的key数量
            Set<String> userKeys = redisTemplate.keys("campus:user:*");
            Set<String> userVOKeys = redisTemplate.keys("campus:user:vo:*");
            Set<String> tokenKeys = redisTemplate.keys("campus:user:token:*");
            Set<String> usernameKeys = redisTemplate.keys("campus:username:*");
            Set<String> searchKeys = redisTemplate.keys("campus:user:search:*");
            Set<String> creditScoreKeys = redisTemplate.keys("campus:user:credit:*");
            
            // 获取帖子相关缓存数量
            Set<String> groupPostsKeys = redisTemplate.keys("campus:group:posts:*");
            Set<String> postDetailKeys = redisTemplate.keys("campus:post:detail:*");
            Set<String> postUserKeys = redisTemplate.keys("campus:post:user:*");
            Set<String> hotPostsKeys = redisTemplate.keys("campus:post:hot");
            
            stats.put("userCacheCount", userKeys != null ? userKeys.size() : 0);
            stats.put("userVOCacheCount", userVOKeys != null ? userVOKeys.size() : 0);
            stats.put("tokenCacheCount", tokenKeys != null ? tokenKeys.size() : 0);
            stats.put("usernameCacheCount", usernameKeys != null ? usernameKeys.size() : 0);
            stats.put("searchCacheCount", searchKeys != null ? searchKeys.size() : 0);
            stats.put("creditScoreCacheCount", creditScoreKeys != null ? creditScoreKeys.size() : 0);
            
            // 添加帖子缓存统计
            stats.put("groupPostsCacheCount", groupPostsKeys != null ? groupPostsKeys.size() : 0);
            stats.put("postDetailCacheCount", postDetailKeys != null ? postDetailKeys.size() : 0);
            stats.put("postUserCacheCount", postUserKeys != null ? postUserKeys.size() : 0);
            stats.put("hotPostsCacheCount", hotPostsKeys != null ? hotPostsKeys.size() : 0);
            
            // 总缓存数量
            Set<String> allKeys = redisTemplate.keys("campus:*");
            stats.put("totalCacheCount", allKeys != null ? allKeys.size() : 0);
            
            // 获取Redis基本信息
            Map<String, Object> redisInfo = new HashMap<>();
            try {
                redisTemplate.execute((RedisConnection connection) -> {
                    Properties info = connection.info();
                    redisInfo.put("version", info.getProperty("redis_version", "未知"));
                    redisInfo.put("uptime", Long.parseLong(info.getProperty("uptime_in_seconds", "0")));
                    redisInfo.put("connectedClients", Integer.parseInt(info.getProperty("connected_clients", "0")));
                    redisInfo.put("usedMemory", Long.parseLong(info.getProperty("used_memory", "0")));
                    
                    // 计算总key数量 (包括非campus前缀的)
                    Long dbSize = connection.dbSize();
                    redisInfo.put("totalKeys", dbSize != null ? dbSize.intValue() : 0);
                    
                    return null;
                });
            } catch (Exception e) {
                log.warn("获取Redis信息失败: {}", e.getMessage());
                redisInfo.put("version", "获取失败");
                redisInfo.put("uptime", 0);
                redisInfo.put("connectedClients", 0);
                redisInfo.put("usedMemory", 0);
                redisInfo.put("totalKeys", 0);
            }
            
            stats.put("redisInfo", redisInfo);
            
            log.info("缓存统计信息: {}", stats);
            return R.ok(stats);
        } catch (Exception e) {
            log.error("获取缓存统计信息失败: {}", e.getMessage());
            return R.fail("获取缓存统计信息失败");
        }
    }

    /**
     * 清空所有缓存
     */
    @DeleteMapping("/clear")
    public R<String> clearAllCache() {
        try {
            Set<String> keys = redisTemplate.keys("campus:*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                log.info("已清空所有缓存，共清理 {} 个key", keys.size());
                return R.ok("缓存清理成功，共清理 " + keys.size() + " 个缓存项");
            } else {
                return R.ok("没有需要清理的缓存");
            }
        } catch (Exception e) {
            log.error("清空缓存失败: {}", e.getMessage());
            return R.fail("清空缓存失败");
        }
    }

    /**
     * 清空用户相关缓存
     */
    @DeleteMapping("/clear/user/{userId}")
    public R<String> clearUserCache(@PathVariable Long userId) {
        try {
            Set<String> keys = redisTemplate.keys("campus:user:" + userId + "*");
            keys.addAll(redisTemplate.keys("campus:user:vo:" + userId + "*"));
            keys.addAll(redisTemplate.keys("campus:user:token:" + userId + "*"));
            
            if (!keys.isEmpty()) {
                redisTemplate.delete(keys);
                log.info("已清空用户{}的缓存，共清理 {} 个key", userId, keys.size());
                return R.ok("用户缓存清理成功，共清理 " + keys.size() + " 个缓存项");
            } else {
                return R.ok("该用户没有缓存数据");
            }
        } catch (Exception e) {
            log.error("清空用户缓存失败: userId={}, {}", userId, e.getMessage());
            return R.fail("清空用户缓存失败");
        }
    }

    /**
     * 清空搜索缓存
     */
    @DeleteMapping("/clear/search")
    public R<String> clearSearchCache() {
        try {
            Set<String> keys = redisTemplate.keys("campus:user:search:*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                log.info("已清空搜索缓存，共清理 {} 个key", keys.size());
                return R.ok("搜索缓存清理成功，共清理 " + keys.size() + " 个缓存项");
            } else {
                return R.ok("没有搜索缓存需要清理");
            }
        } catch (Exception e) {
            log.error("清空搜索缓存失败: {}", e.getMessage());
            return R.fail("清空搜索缓存失败");
        }
    }
    
    /**
     * 清空信用分缓存
     */
    @DeleteMapping("/clear/credit")
    public R<String> clearCreditScoreCache() {
        try {
            Set<String> keys = redisTemplate.keys("campus:user:credit:*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                log.info("已清空信用分缓存，共清理 {} 个key", keys.size());
                return R.ok("信用分缓存清理成功，共清理 " + keys.size() + " 个缓存项");
            } else {
                return R.ok("没有信用分缓存需要清理");
            }
        } catch (Exception e) {
            log.error("清空信用分缓存失败: {}", e.getMessage());
            return R.fail("清空信用分缓存失败");
        }
    }
    
    /**
     * 清空指定用户的信用分缓存
     */
    @DeleteMapping("/clear/credit/{userId}")
    public R<String> clearUserCreditScoreCache(@PathVariable Long userId) {
        try {
            String key = "campus:user:credit:" + userId;
            Boolean hasKey = redisTemplate.hasKey(key);
            if (hasKey != null && hasKey) {
                redisTemplate.delete(key);
                log.info("已清空用户{}的信用分缓存", userId);
                return R.ok("用户信用分缓存清理成功");
            } else {
                return R.ok("该用户没有信用分缓存数据");
            }
        } catch (Exception e) {
            log.error("清空用户信用分缓存失败: userId={}, {}", userId, e.getMessage());
            return R.fail("清空用户信用分缓存失败");
        }
    }

    /**
     * 清空所有帖子缓存
     */
    @DeleteMapping("/clear/posts")
    public R<String> clearAllPostCache() {
        try {
            Set<String> keys = new HashSet<>();
            
            // 收集所有帖子相关的缓存key
            Set<String> groupPostsKeys = redisTemplate.keys("campus:group:posts:*");
            Set<String> postDetailKeys = redisTemplate.keys("campus:post:detail:*");
            Set<String> postUserKeys = redisTemplate.keys("campus:post:user:*");
            Set<String> hotPostsKeys = redisTemplate.keys("campus:post:hot");
            
            if (groupPostsKeys != null) keys.addAll(groupPostsKeys);
            if (postDetailKeys != null) keys.addAll(postDetailKeys);
            if (postUserKeys != null) keys.addAll(postUserKeys);
            if (hotPostsKeys != null) keys.addAll(hotPostsKeys);
            
            if (!keys.isEmpty()) {
                redisTemplate.delete(keys);
                log.info("已清空所有帖子缓存，共清理 {} 个key", keys.size());
                return R.ok("帖子缓存清理成功，共清理 " + keys.size() + " 个缓存项");
            } else {
                return R.ok("没有帖子缓存需要清理");
            }
        } catch (Exception e) {
            log.error("清空帖子缓存失败: {}", e.getMessage());
            return R.fail("清空帖子缓存失败");
        }
    }
    
    /**
     * 清空指定小组的帖子缓存
     */
    @DeleteMapping("/clear/group-posts/{groupId}")
    public R<String> clearGroupPostsCache(@PathVariable Long groupId) {
        try {
            postCacheService.evictGroupPostsCache(groupId);
            log.info("已清空小组{}的帖子缓存", groupId);
            return R.ok("小组帖子缓存清理成功");
        } catch (Exception e) {
            log.error("清空小组帖子缓存失败: groupId={}, {}", groupId, e.getMessage());
            return R.fail("清空小组帖子缓存失败");
        }
    }
    
    /**
     * 清空指定帖子详情缓存
     */
    @DeleteMapping("/clear/post-detail/{postId}")
    public R<String> clearPostDetailCache(@PathVariable Long postId) {
        try {
            postCacheService.evictPostDetailCache(postId);
            log.info("已清空帖子{}的详情缓存", postId);
            return R.ok("帖子详情缓存清理成功");
        } catch (Exception e) {
            log.error("清空帖子详情缓存失败: postId={}, {}", postId, e.getMessage());
            return R.fail("清空帖子详情缓存失败");
        }
    }
    
    /**
     * 清空热门帖子缓存
     */
    @DeleteMapping("/clear/hot-posts")
    public R<String> clearHotPostsCache() {
        try {
            postCacheService.evictHotPostsCache();
            log.info("已清空热门帖子缓存");
            return R.ok("热门帖子缓存清理成功");
        } catch (Exception e) {
            log.error("清空热门帖子缓存失败: {}", e.getMessage());
            return R.fail("清空热门帖子缓存失败");
        }
    }

    /**
     * 测试Redis连接
     */
    @GetMapping("/test")
    public R<String> testRedisConnection() {
        try {
            // 设置一个测试key
            String testKey = "campus:test:connection";
            String testValue = "Redis连接正常 - " + System.currentTimeMillis();
            redisTemplate.opsForValue().set(testKey, testValue);
            
            // 读取测试key
            String retrievedValue = (String) redisTemplate.opsForValue().get(testKey);
            
            // 删除测试key
            redisTemplate.delete(testKey);
            
            if (testValue.equals(retrievedValue)) {
                log.info("Redis连接测试成功");
                return R.ok("Redis连接正常");
            } else {
                log.error("Redis连接测试失败: 写入和读取的值不匹配");
                return R.fail("Redis连接异常");
            }
        } catch (Exception e) {
            log.error("Redis连接测试失败: {}", e.getMessage());
            return R.fail("Redis连接失败: " + e.getMessage());
        }
    }

    /**
     * 获取缓存详细信息
     */
    @GetMapping("/details")
    public R<Map<String, Object>> getCacheDetails() {
        try {
            Map<String, Object> details = new HashMap<>();
            
            // 获取所有缓存key
            Set<String> allKeys = redisTemplate.keys("campus:*");
            if (allKeys != null) {
                details.put("allKeys", new ArrayList<>(allKeys));
                
                // 按类型分组
                Map<String, Integer> typeCount = new HashMap<>();
                Map<String, Map<String, Object>> keyDetails = new HashMap<>();
                
                for (String key : allKeys) {
                    try {
                        // 分析key类型
                        String[] parts = key.split(":");
                        String type = parts.length > 1 ? parts[1] : "unknown";
                        typeCount.put(type, typeCount.getOrDefault(type, 0) + 1);
                        
                        // 获取key的详细信息
                        Map<String, Object> keyInfo = new HashMap<>();
                        
                        // 获取TTL
                        Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
                        keyInfo.put("ttl", ttl != null ? ttl : -1);
                        
                        // 获取数据类型
                        String dataType = redisTemplate.type(key).code();
                        keyInfo.put("type", dataType);
                        
                        // 估算大小 (简单实现)
                        try {
                            Object value = redisTemplate.opsForValue().get(key);
                            if (value != null) {
                                keyInfo.put("size", value.toString().length());
                            } else {
                                keyInfo.put("size", 0);
                            }
                        } catch (Exception e) {
                            keyInfo.put("size", -1); // 无法获取大小
                        }
                        
                        keyDetails.put(key, keyInfo);
                    } catch (Exception e) {
                        log.warn("获取key详细信息失败: {}, {}", key, e.getMessage());
                    }
                }
                
                details.put("typeCount", typeCount);
                details.put("keyDetails", keyDetails);
            } else {
                details.put("allKeys", new ArrayList<>());
                details.put("typeCount", new HashMap<>());
                details.put("keyDetails", new HashMap<>());
            }
            
            return R.ok(details);
        } catch (Exception e) {
            log.error("获取缓存详细信息失败: {}", e.getMessage());
            return R.fail("获取缓存详细信息失败");
        }
    }
}
