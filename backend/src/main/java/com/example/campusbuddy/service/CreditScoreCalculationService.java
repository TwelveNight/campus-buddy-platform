package com.example.campusbuddy.service;

import com.example.campusbuddy.entity.Review;
import com.example.campusbuddy.entity.User;

import java.util.List;

/**
 * 信用积分计算服务接口
 */
public interface CreditScoreCalculationService {
    
    /**
     * 计算用户的信用积分
     * @param userId 用户ID
     * @return 计算后的信用积分（0-100分制）
     */
    Integer calculateCreditScore(Long userId);
    
    /**
     * 检查用户信用分是否存在于缓存中
     * @param userId 用户ID
     * @return 是否存在于缓存
     */
    boolean existsInCache(Long userId);
    
    /**
     * 增量更新用户信用积分（用于新增评价时的快速更新）
     * @param userId 用户ID
     * @param newReview 新增的评价
     * @return 更新后的信用积分
     */
    Integer incrementalUpdateCreditScore(Long userId, Review newReview);
    
    /**
     * 批量计算多个用户的信用积分
     * @param userIds 用户ID列表
     * @return 用户ID到信用积分的映射
     */
    java.util.Map<Long, Integer> batchCalculateCreditScore(List<Long> userIds);
    
    /**
     * 获取用户的信用等级
     * @param creditScore 信用积分
     * @return 信用等级描述
     */
    String getCreditLevel(Integer creditScore);
    
    /**
     * 计算信用积分统计信息
     * @param userId 用户ID
     * @return 信用积分统计信息
     */
    CreditScoreStats getCreditScoreStats(Long userId);
    
    /**
     * 信用积分统计信息
     */
    class CreditScoreStats {
        private Integer creditScore;
        private String creditLevel;
        private Integer totalReviews;
        private Double averageScore;
        private Integer recentReviews; // 最近30天的评价数
        private Double recentAverageScore; // 最近30天的平均分
        private String trend; // 趋势：上升、下降、稳定
        
        // 构造函数
        public CreditScoreStats() {}
        
        public CreditScoreStats(Integer creditScore, String creditLevel, Integer totalReviews, 
                               Double averageScore, Integer recentReviews, Double recentAverageScore, String trend) {
            this.creditScore = creditScore;
            this.creditLevel = creditLevel;
            this.totalReviews = totalReviews;
            this.averageScore = averageScore;
            this.recentReviews = recentReviews;
            this.recentAverageScore = recentAverageScore;
            this.trend = trend;
        }
        
        // Getters and Setters
        public Integer getCreditScore() { return creditScore; }
        public void setCreditScore(Integer creditScore) { this.creditScore = creditScore; }
        
        public String getCreditLevel() { return creditLevel; }
        public void setCreditLevel(String creditLevel) { this.creditLevel = creditLevel; }
        
        public Integer getTotalReviews() { return totalReviews; }
        public void setTotalReviews(Integer totalReviews) { this.totalReviews = totalReviews; }
        
        public Double getAverageScore() { return averageScore; }
        public void setAverageScore(Double averageScore) { this.averageScore = averageScore; }
        
        public Integer getRecentReviews() { return recentReviews; }
        public void setRecentReviews(Integer recentReviews) { this.recentReviews = recentReviews; }
        
        public Double getRecentAverageScore() { return recentAverageScore; }
        public void setRecentAverageScore(Double recentAverageScore) { this.recentAverageScore = recentAverageScore; }
        
        public String getTrend() { return trend; }
        public void setTrend(String trend) { this.trend = trend; }
    }
}
