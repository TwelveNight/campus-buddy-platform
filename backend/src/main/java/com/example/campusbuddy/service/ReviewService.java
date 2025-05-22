package com.example.campusbuddy.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.campusbuddy.entity.Review;

import java.util.List;

public interface ReviewService extends IService<Review> {
    // 提交评价并自动更新信用分
    boolean submitReview(Review review);
    // 查询某个互助的评价
    List<Review> getReviewsByHelpId(Long helpId);
    // 查询某个用户收到的评价
    List<Review> getReviewsByUserId(Long userId);
    // 检查用户是否已对特定互助进行评价
    boolean hasReviewed(Long reviewerUserId, Long helpId);
    // 获取用户的信用分
    Integer getUserCreditScore(Long userId);
}
