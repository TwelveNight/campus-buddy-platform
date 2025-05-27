package com.example.campusbuddy.controller;

import com.example.campusbuddy.common.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * 缓存管理控制器
 * 用于监控和管理Redis缓存
 */
@RestController
@RequestMapping("/api/cache")
public class CacheController {

    private static final Logger log = Logger.getLogger(CacheController.class.getName());

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

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
            
            stats.put("userCacheCount", userKeys != null ? userKeys.size() : 0);
            stats.put("userVOCacheCount", userVOKeys != null ? userVOKeys.size() : 0);
            stats.put("tokenCacheCount", tokenKeys != null ? tokenKeys.size() : 0);
            stats.put("usernameCacheCount", usernameKeys != null ? usernameKeys.size() : 0);
            stats.put("searchCacheCount", searchKeys != null ? searchKeys.size() : 0);
            
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
                log.warning("获取Redis信息失败: " + e.getMessage());
                redisInfo.put("version", "获取失败");
                redisInfo.put("uptime", 0);
                redisInfo.put("connectedClients", 0);
                redisInfo.put("usedMemory", 0);
                redisInfo.put("totalKeys", 0);
            }
            
            stats.put("redisInfo", redisInfo);
            
            log.info("缓存统计信息: " + stats);
            return R.ok(stats);
        } catch (Exception e) {
            log.severe("获取缓存统计信息失败: " + e.getMessage());
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
                log.info("已清空所有缓存，共清理 " + keys.size() + " 个key");
                return R.ok("缓存清理成功，共清理 " + keys.size() + " 个缓存项");
            } else {
                return R.ok("没有需要清理的缓存");
            }
        } catch (Exception e) {
            log.severe("清空缓存失败: " + e.getMessage());
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
                log.info("已清空用户" + userId + "的缓存，共清理 " + keys.size() + " 个key");
                return R.ok("用户缓存清理成功，共清理 " + keys.size() + " 个缓存项");
            } else {
                return R.ok("该用户没有缓存数据");
            }
        } catch (Exception e) {
            log.severe("清空用户缓存失败: userId=" + userId + ", " + e.getMessage());
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
                log.info("已清空搜索缓存，共清理 " + keys.size() + " 个key");
                return R.ok("搜索缓存清理成功，共清理 " + keys.size() + " 个缓存项");
            } else {
                return R.ok("没有搜索缓存需要清理");
            }
        } catch (Exception e) {
            log.severe("清空搜索缓存失败: " + e.getMessage());
            return R.fail("清空搜索缓存失败");
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
                log.severe("Redis连接测试失败: 写入和读取的值不匹配");
                return R.fail("Redis连接异常");
            }
        } catch (Exception e) {
            log.severe("Redis连接测试失败: " + e.getMessage());
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
                        log.warning("获取key详细信息失败: " + key + ", " + e.getMessage());
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
            log.severe("获取缓存详细信息失败: " + e.getMessage());
            return R.fail("获取缓存详细信息失败");
        }
    }
}
