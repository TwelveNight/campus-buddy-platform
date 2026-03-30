package com.example.campusbuddy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.campusbuddy.entity.Review;
import com.example.campusbuddy.mapper.ReviewMapper;
import com.example.campusbuddy.service.CreditScoreCalculationService;
import com.example.campusbuddy.service.UserCacheService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 信用积分计算服务实现
 * <p>
 * 采用贝叶斯平均算法：每位用户初始拥有 PRIOR_COUNT 条 PRIOR_STAR 星的虚拟评价作为先验，
 * 随着真实评价增加，虚拟评价被逐渐"稀释"，最终分数趋近于真实平均水平。
 * 公式：bayesianStar = (n × avgStar + m × priorStar) / (n + m)
 *       finalScore  = round(bayesianStar / 5.0 × 100)
 */
@Service
@Slf4j
public class CreditScoreCalculationServiceImpl implements CreditScoreCalculationService {

    @Autowired
    private ReviewMapper reviewMapper;

    @Autowired
    private UserCacheService userCacheService;

    // 基础配置
    private static final int BASE_CREDIT_SCORE = 70; // 无评价时的基础分（对应 3.5星先验）
    private static final int MAX_CREDIT_SCORE = 100;
    private static final int MIN_CREDIT_SCORE = 0;
    private static final long CACHE_EXPIRE_SECONDS = 3600; // 信用积分缓存1小时

    // 贝叶斯先验参数
    private static final double PRIOR_STAR = 3.5;  // 先验星级（3.5星 = 70分）
    private static final int    PRIOR_COUNT = 10;  // 先验强度（相当于10条虚拟评价）

    // 评价类型权重：发布者对帮助者的评价更能反映服务质量，权重略高
    private static final double WEIGHT_PUBLISHER_TO_HELPER = 1.2;
    private static final double WEIGHT_HELPER_TO_PUBLISHER = 1.0;
    
    @Override
    public Integer calculateCreditScore(Long userId) {
        log.info("开始计算用户信用积分: userId={}", userId);

        // 先从缓存获取
        Integer cachedScore = userCacheService.getCachedCreditScore(userId);
        if (cachedScore != null) {
            log.debug("从缓存获取信用积分: userId={}, score={}", userId, cachedScore);
            return cachedScore;
        }

        // 获取用户所有评价
        List<Review> userReviews = reviewMapper.selectList(
            new QueryWrapper<Review>().eq("reviewed_user_id", userId)
        );

        if (userReviews.isEmpty()) {
            log.info("用户暂无评价记录，返回基础信用分: userId={}, baseScore={}", userId, BASE_CREDIT_SCORE);
            userCacheService.cacheCreditScore(userId, BASE_CREDIT_SCORE, CACHE_EXPIRE_SECONDS);
            return BASE_CREDIT_SCORE;
        }

        // 贝叶斯加权平均：不同评价类型赋予不同权重
        // PUBLISHER_TO_HELPER（别人评价你的帮助质量）权重 1.2，更能反映服务能力
        // HELPER_TO_PUBLISHER（别人评价你作为求助方的态度）权重 1.0
        double weightedStarSum = 0.0;
        double totalWeight = 0.0;
        for (Review r : userReviews) {
            double w = "PUBLISHER_TO_HELPER".equals(r.getReviewType())
                    ? WEIGHT_PUBLISHER_TO_HELPER : WEIGHT_HELPER_TO_PUBLISHER;
            weightedStarSum += r.getScore() * w;
            totalWeight += w;
        }
        double weightedAvgStar = totalWeight > 0 ? weightedStarSum / totalWeight : PRIOR_STAR;

        // 贝叶斯平均：用等效评价数量（totalWeight）代替评价条数
        double bayesianStar = (weightedStarSum + PRIOR_COUNT * PRIOR_STAR) / (totalWeight + PRIOR_COUNT);

        // 映射到 0-100 分制
        int finalScore = (int) Math.round(bayesianStar / 5.0 * 100);
        finalScore = Math.max(MIN_CREDIT_SCORE, Math.min(MAX_CREDIT_SCORE, finalScore));

        log.info("信用积分计算完成: userId={}, finalScore={}, reviewCount={}, weightedAvgStar={}, bayesianStar={}",
                 userId, finalScore, userReviews.size(), weightedAvgStar, bayesianStar);

        userCacheService.cacheCreditScore(userId, finalScore, CACHE_EXPIRE_SECONDS);
        return finalScore;
    }
    
    @Override
    public boolean existsInCache(Long userId) {
        if (userId == null) {
            return false;
        }
        return userCacheService.getCachedCreditScore(userId) != null;
    }
    
    @Override
    public Integer incrementalUpdateCreditScore(Long userId, Review newReview) {
        log.info("增量更新用户信用积分: userId={}, newReviewId={}", userId, newReview.getReviewId());
        
        // 清除缓存，强制重新计算
        userCacheService.evictCreditScoreCache(userId);
        
        // 重新计算信用积分
        return calculateCreditScore(userId);
    }
    
    @Override
    public Map<Long, Integer> batchCalculateCreditScore(List<Long> userIds) {
        log.info("批量计算信用积分: userCount={}", userIds.size());
        
        Map<Long, Integer> result = new HashMap<>();
        
        // 先从缓存批量获取
        Map<Long, Integer> cachedScores = userCacheService.getBatchCachedCreditScores(userIds);
        result.putAll(cachedScores);
        
        // 计算缓存中没有的用户
        List<Long> uncachedUserIds = userIds.stream()
            .filter(userId -> !cachedScores.containsKey(userId))
            .collect(Collectors.toList());
        
        if (!uncachedUserIds.isEmpty()) {
            Map<Long, Integer> newScores = new HashMap<>();
            for (Long userId : uncachedUserIds) {
                Integer score = calculateCreditScore(userId);
                newScores.put(userId, score);
                result.put(userId, score);
            }
            
            // 批量缓存新计算的分数
            userCacheService.batchCacheCreditScores(newScores, CACHE_EXPIRE_SECONDS);
        }
        
        return result;
    }
    
    @Override
    public String getCreditLevel(Integer creditScore) {
        if (creditScore == null) return "未评级";

        if (creditScore >= 90) return "优秀";
        else if (creditScore >= 80) return "良好";
        else if (creditScore >= 70) return "中等";
        else if (creditScore >= 60) return "及格";
        else return "待提升";
    }
    
    @Override
    public CreditScoreStats getCreditScoreStats(Long userId) {
        log.info("获取用户信用积分统计: userId={}", userId);
        
        // 获取所有评价
        List<Review> allReviews = reviewMapper.selectList(
            new QueryWrapper<Review>().eq("reviewed_user_id", userId).orderByDesc("created_at")
        );
        
        if (allReviews.isEmpty()) {
            return new CreditScoreStats(BASE_CREDIT_SCORE, getCreditLevel(BASE_CREDIT_SCORE), 
                                       0, 0.0, 0, 0.0, "稳定");
        }
        
        // 计算当前信用分
        Integer creditScore = calculateCreditScore(userId);
        String creditLevel = getCreditLevel(creditScore);
        
        // 计算总体统计
        int totalReviews = allReviews.size();
        double averageScore = allReviews.stream().mapToInt(review -> review.getScore()).average().orElse(0.0);
        
        // 计算最近30天统计
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minus(30, ChronoUnit.DAYS);
        List<Review> recentReviews = allReviews.stream()
            .filter(review -> {
                LocalDateTime createdTime = review.getCreatedAt().toInstant()
                    .atZone(java.time.ZoneId.systemDefault()).toLocalDateTime();
                return createdTime.isAfter(thirtyDaysAgo);
            })
            .collect(Collectors.toList());
        
        int recentReviewCount = recentReviews.size();
        double recentAverageScore = recentReviews.stream().mapToInt(review -> review.getScore()).average().orElse(0.0);
        
        // 计算趋势
        String trend = calculateTrend(allReviews);
        
        return new CreditScoreStats(creditScore, creditLevel, totalReviews, averageScore, 
                                   recentReviewCount, recentAverageScore, trend);
    }
    
    /**
     * 计算信用分趋势
     * 通过比较最近一段时间和之前一段时间的评价，分析用户信用分的变化趋势
     */
    private String calculateTrend(List<Review> reviews) {
        if (reviews.size() < 5) return "稳定"; // 评价太少无法确定趋势
        
        // 确保评价按时间降序排序（最新的在前面）
        List<Review> sortedReviews = new ArrayList<>(reviews);
        sortedReviews.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
        
        // 获取最近30天的评价
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minus(30, ChronoUnit.DAYS);
        List<Review> recentReviews = new ArrayList<>();
        List<Review> olderReviews = new ArrayList<>();
        
        for (Review review : sortedReviews) {
            LocalDateTime createdTime = review.getCreatedAt().toInstant()
                .atZone(java.time.ZoneId.systemDefault()).toLocalDateTime();
                
            if (createdTime.isAfter(thirtyDaysAgo)) {
                recentReviews.add(review);
            } else {
                olderReviews.add(review);
            }
        }
        
        // 如果最近没有评价，返回稳定
        if (recentReviews.isEmpty()) return "稳定";
        
        // 如果只有最近的评价，没有历史评价，返回稳定
        if (olderReviews.isEmpty()) return "稳定";
        
        // 计算最近评价的加权平均分
        double recentAvg = recentReviews.stream()
            .mapToDouble(Review::getScore)
            .average()
            .orElse(0.0);
        
        // 计算较早评价的加权平均分
        double olderAvg = olderReviews.stream()
            .mapToDouble(Review::getScore)
            .average()
            .orElse(0.0);
        
        // 计算最近评分与历史评分的差值
        double diff = recentAvg - olderAvg;
        
        // 评价波动显著性阈值
        double significantChangeThreshold = 0.3;
        double highChangeThreshold = 0.8;
        
        log.debug("信用分趋势分析: recentReviewCount={}, olderReviewCount={}, recentAvg={}, olderAvg={}, diff={}",
                 recentReviews.size(), olderReviews.size(), recentAvg, olderAvg, diff);
        
        // 根据差值确定趋势
        if (diff > highChangeThreshold) {
            return "显著上升";
        } else if (diff > significantChangeThreshold) {
            return "上升";
        } else if (diff < -highChangeThreshold) {
            return "显著下降";
        } else if (diff < -significantChangeThreshold) {
            return "下降";
        } else {
            return "稳定";
        }
    }
}
