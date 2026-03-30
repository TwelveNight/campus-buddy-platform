package com.example.campusbuddy.service.impl;

import com.example.campusbuddy.entity.Review;
import com.example.campusbuddy.entity.User;
import com.example.campusbuddy.mapper.ReviewMapper;
import com.example.campusbuddy.service.CreditScoreCalculationService;
import com.example.campusbuddy.service.UserCacheService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 信用积分计算服务实现
 * <p>
 * 采用贝叶斯平均算法：每位用户初始拥有 PRIOR_COUNT 条 PRIOR_STAR 星的虚拟评价作为先验，
 * 随着真实评价增加，虚拟评价被逐渐"稀释"，最终分数趋近于真实平均水平。
 * 公式：bayesianStar = (Σ(star×w) + m×priorStar) / (Σw + m)
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
    private static final int BASE_CREDIT_SCORE = 70;
    private static final int MAX_CREDIT_SCORE = 100;
    private static final int MIN_CREDIT_SCORE = 0;

    // 贝叶斯先验参数
    private static final double PRIOR_STAR = 3.5;  // 先验星级（3.5星 = 70分）
    private static final int    PRIOR_COUNT = 10;  // 先验强度（相当于10条虚拟评价）

    // 评价类型权重：发布者对帮助者的评价更能反映服务质量，权重略高
    private static final double WEIGHT_PUBLISHER_TO_HELPER = 1.2;
    private static final double WEIGHT_HELPER_TO_PUBLISHER = 1.0;

    @Override
    public Integer calculateCreditScore(Long userId) {
        log.info("开始计算用户信用积分: userId={}", userId);

        // 从用户缓存读取 creditScore（避免独立维护一套 credit 缓存）
        User cachedUser = userCacheService.getCachedUser(userId);
        if (cachedUser != null && cachedUser.getCreditScore() != null) {
            log.debug("从用户缓存获取信用积分: userId={}, score={}", userId, cachedUser.getCreditScore());
            return cachedUser.getCreditScore();
        }

        // 获取用户所有评价
        List<Review> userReviews = reviewMapper.selectByReviewedUserId(userId);

        if (userReviews.isEmpty()) {
            log.info("用户暂无评价记录，返回基础信用分: userId={}, baseScore={}", userId, BASE_CREDIT_SCORE);
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

        return finalScore;
    }

    @Override
    public boolean existsInCache(Long userId) {
        if (userId == null) return false;
        User cached = userCacheService.getCachedUser(userId);
        return cached != null && cached.getCreditScore() != null;
    }

    @Override
    public Integer incrementalUpdateCreditScore(Long userId, Review newReview) {
        log.info("增量更新用户信用积分: userId={}, newReviewId={}", userId, newReview.getReviewId());
        // 清除用户缓存，下次读取时从 DB 重建（含新的 creditScore）
        userCacheService.evictUserCache(userId);
        return calculateCreditScore(userId);
    }

    @Override
    public Map<Long, Integer> batchCalculateCreditScore(List<Long> userIds) {
        log.info("批量计算信用积分: userCount={}", userIds.size());
        Map<Long, Integer> result = new HashMap<>();
        for (Long userId : userIds) {
            result.put(userId, calculateCreditScore(userId));
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

        List<Review> allReviews = reviewMapper.selectByReviewedUserId(userId);

        if (allReviews.isEmpty()) {
            return new CreditScoreStats(BASE_CREDIT_SCORE, getCreditLevel(BASE_CREDIT_SCORE), 0, 0.0);
        }

        Integer creditScore = calculateCreditScore(userId);
        String creditLevel = getCreditLevel(creditScore);
        int totalReviews = allReviews.size();
        double averageScore = allReviews.stream().mapToInt(Review::getScore).average().orElse(0.0);

        return new CreditScoreStats(creditScore, creditLevel, totalReviews, averageScore);
    }
}
