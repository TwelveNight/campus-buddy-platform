package com.example.campusbuddy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.campusbuddy.entity.Review;
import com.example.campusbuddy.entity.User;
import com.example.campusbuddy.mapper.ReviewMapper;
import com.example.campusbuddy.mapper.UserMapper;
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
 */
@Service
@Slf4j
public class CreditScoreCalculationServiceImpl implements CreditScoreCalculationService {
    
    @Autowired
    private ReviewMapper reviewMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private UserCacheService userCacheService;
    
    // 配置参数
    private static final int BASE_CREDIT_SCORE = 60; // 基础信用分
    private static final int MAX_CREDIT_SCORE = 100; // 最高信用分
    private static final int MIN_CREDIT_SCORE = 0; // 最低信用分
    private static final long CACHE_EXPIRE_SECONDS = 3600; // 信用积分缓存1小时
    
    // 权重配置
    private static final double TIME_DECAY_FACTOR = 0.1; // 时间衰减因子
    private static final double REVIEW_COUNT_WEIGHT = 0.2; // 评价数量权重
    private static final double REVIEWER_CREDIT_WEIGHT = 0.1; // 评价者信用权重
    
    // 模块权重配置
    private static final Map<String, Double> MODULE_WEIGHTS = Map.of(
        "HELP", 1.0,
        "COURSE_TUTORING", 1.2,
        "SKILL_LEARNING", 1.1,
        "ITEM_LEND", 0.9,
        "GROUP", 1.0,
        "RESOURCE", 0.8
    );
    
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
            new QueryWrapper<Review>().eq("reviewed_user_id", userId).orderByDesc("created_at")
        );
        
        if (userReviews.isEmpty()) {
            log.info("用户暂无评价记录，返回基础信用分: userId={}, baseScore={}", userId, BASE_CREDIT_SCORE);
            // 缓存基础信用分
            userCacheService.cacheCreditScore(userId, BASE_CREDIT_SCORE, CACHE_EXPIRE_SECONDS);
            return BASE_CREDIT_SCORE;
        }
        
        // 计算加权信用分
        double weightedScore = calculateWeightedScore(userReviews);
        
        // 应用评价数量加分
        double reviewCountBonus = calculateReviewCountBonus(userReviews.size());
        
        // 计算最终信用分
        int finalScore = (int) Math.round(BASE_CREDIT_SCORE + weightedScore + reviewCountBonus);
        finalScore = Math.max(MIN_CREDIT_SCORE, Math.min(MAX_CREDIT_SCORE, finalScore));
        
        log.info("信用积分计算完成: userId={}, finalScore={}, reviewCount={}, weightedScore={}, countBonus={}", 
                 userId, finalScore, userReviews.size(), weightedScore, reviewCountBonus);
        
        // 缓存计算结果
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
     * 计算加权评分
     * 综合考虑评价的时间衰减、模块权重、评价者信用等因素，计算加权评分
     */
    private double calculateWeightedScore(List<Review> reviews) {
        if (reviews.isEmpty()) return 0.0;
        
        double totalWeight = 0.0;
        double weightedSum = 0.0;
        LocalDateTime now = LocalDateTime.now();
        
        // 按时间排序，优先考虑最近的评价
        List<Review> sortedReviews = new ArrayList<>(reviews);
        sortedReviews.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
        
        for (Review review : sortedReviews) {
            try {
                // 基础分数（转换为-20到+20的范围）
                // 1分=-20分，3分=0分，5分=+20分
                double baseScore = ((review.getScore() - 3.0) / 2.0) * 20;
                
                // 时间权重计算（越近的评价权重越高）
                LocalDateTime createdTime = review.getCreatedAt().toInstant()
                    .atZone(java.time.ZoneId.systemDefault()).toLocalDateTime();
                long daysAgo = ChronoUnit.DAYS.between(createdTime, now);
                
                // 时间衰减函数：一年后权重降低到约37%，两年后约14%
                double timeWeight = Math.exp(-daysAgo * TIME_DECAY_FACTOR / 365);
                
                // 模块权重 - 根据不同模块的重要性设定不同权重
                double moduleWeight = MODULE_WEIGHTS.getOrDefault(review.getModuleType(), 1.0);
                
                // 评价者信用权重 - 信用分高的用户评价更可信
                double reviewerWeight = getReviewerCreditWeight(review.getReviewerUserId());
                
                // 评价内容长度权重 - 有内容的评价比无内容的评价更有参考价值
                double contentWeight = 1.0;
                if (review.getContent() != null && !review.getContent().trim().isEmpty()) {
                    // 有详细评价内容的权重略高
                    contentWeight = 1.2;
                }
                
                // 综合权重计算
                double finalWeight = timeWeight * moduleWeight * reviewerWeight * contentWeight;
                
                // 记录日志，便于调试
                if (log.isDebugEnabled()) {
                    log.debug("评价加权计算: reviewId={}, score={}, baseScore={}, timeWeight={}, moduleWeight={}, " +
                             "reviewerWeight={}, contentWeight={}, finalWeight={}",
                             review.getReviewId(), review.getScore(), baseScore, timeWeight, 
                             moduleWeight, reviewerWeight, contentWeight, finalWeight);
                }
                
                // 累加加权分数
                weightedSum += baseScore * finalWeight;
                totalWeight += finalWeight;
            } catch (Exception e) {
                // 单个评价计算出错不应影响整体计算
                log.warn("计算单个评价权重出错: reviewId={}", review.getReviewId(), e);
            }
        }
        
        // 返回加权平均分
        double result = totalWeight > 0 ? weightedSum / totalWeight : 0.0;
        log.debug("加权分数计算结果: weightedSum={}, totalWeight={}, result={}", weightedSum, totalWeight, result);
        return result;
    }
    
    /**
     * 计算评价数量加分
     */
    private double calculateReviewCountBonus(int reviewCount) {
        // 使用对数函数，让加分随评价数量增长但增长率递减
        if (reviewCount <= 0) return 0;
        
        // 最多加20分，评价数量达到100时接近满分
        return Math.min(20, Math.log(reviewCount + 1) * 5);
    }
    
    /**
     * 获取评价者信用权重
     */
    private double getReviewerCreditWeight(Long reviewerUserId) {
        try {
            // 从缓存获取评价者信用分
            Integer reviewerCredit = userCacheService.getCachedCreditScore(reviewerUserId);
            if (reviewerCredit == null) {
                // 如果缓存中没有，从数据库获取
                User reviewer = userMapper.selectById(reviewerUserId);
                reviewerCredit = reviewer != null ? reviewer.getCreditScore() : BASE_CREDIT_SCORE;
            }
            
            // 将信用分转换为权重（60分=1.0，100分=1.4，0分=0.6）
            return 0.6 + (reviewerCredit / 100.0) * 0.8;
        } catch (Exception e) {
            log.warn("获取评价者信用权重失败: reviewerUserId={}", reviewerUserId, e);
            return 1.0; // 默认权重
        }
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
