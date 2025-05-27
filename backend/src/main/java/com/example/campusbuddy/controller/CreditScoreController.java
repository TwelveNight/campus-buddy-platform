package com.example.campusbuddy.controller;

import com.example.campusbuddy.service.CreditScoreCalculationService;
import com.example.campusbuddy.service.CreditScoreCalculationService.CreditScoreStats;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "信用积分接口", description = "信用积分计算和统计相关操作")
@RestController
@RequestMapping("/api/credit")
@Slf4j
public class CreditScoreController {
    
    @Autowired
    private CreditScoreCalculationService creditScoreCalculationService;
    
    @Operation(summary = "获取用户信用积分")
    @GetMapping("/score/{userId}")
    public ResponseEntity<Integer> getCreditScore(@PathVariable Long userId) {
        try {
            // 检查缓存中是否已存在信用分
            boolean existsInCache = creditScoreCalculationService.existsInCache(userId);
            Integer creditScore = creditScoreCalculationService.calculateCreditScore(userId);
            log.info("获取用户信用积分: userId={}, score={}, fromCache={}", userId, creditScore, existsInCache);
            return ResponseEntity.ok(creditScore);
        } catch (Exception e) {
            log.error("获取用户信用积分失败: userId={}", userId, e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    @Operation(summary = "获取用户信用等级")
    @GetMapping("/level/{userId}")
    public ResponseEntity<String> getCreditLevel(@PathVariable Long userId) {
        try {
            Integer creditScore = creditScoreCalculationService.calculateCreditScore(userId);
            String creditLevel = creditScoreCalculationService.getCreditLevel(creditScore);
            return ResponseEntity.ok(creditLevel);
        } catch (Exception e) {
            log.error("获取用户信用等级失败: userId={}", userId, e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    @Operation(summary = "获取用户信用积分详细统计")
    @GetMapping("/stats/{userId}")
    public ResponseEntity<CreditScoreStats> getCreditScoreStats(@PathVariable Long userId) {
        try {
            boolean existsInCache = creditScoreCalculationService.existsInCache(userId);
            CreditScoreStats stats = creditScoreCalculationService.getCreditScoreStats(userId);
            log.info("获取用户信用积分统计: userId={}, creditScore={}, fromCache={}", 
                     userId, stats.getCreditScore(), existsInCache);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("获取用户信用积分统计失败: userId={}", userId, e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    @Operation(summary = "批量获取用户信用积分")
    @PostMapping("/batch")
    public ResponseEntity<Map<Long, Integer>> getBatchCreditScores(@RequestBody List<Long> userIds) {
        try {
            if (userIds == null || userIds.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            
            Map<Long, Integer> creditScores = creditScoreCalculationService.batchCalculateCreditScore(userIds);
            return ResponseEntity.ok(creditScores);
        } catch (Exception e) {
            log.error("批量获取用户信用积分失败: userIds={}", userIds, e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    @Operation(summary = "强制重新计算用户信用积分")
    @PostMapping("/recalculate/{userId}")
    public ResponseEntity<Integer> recalculateCreditScore(@PathVariable Long userId) {
        try {
            // 清除缓存，强制重新计算
            Integer creditScore = creditScoreCalculationService.calculateCreditScore(userId);
            log.info("强制重新计算用户信用积分: userId={}, newScore={}", userId, creditScore);
            return ResponseEntity.ok(creditScore);
        } catch (Exception e) {
            log.error("强制重新计算用户信用积分失败: userId={}", userId, e);
            return ResponseEntity.badRequest().build();
        }
    }
}
