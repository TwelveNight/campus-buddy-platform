package com.example.campusbuddy.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.campusbuddy.entity.Review;
import com.example.campusbuddy.vo.ReviewVO;
import com.example.campusbuddy.vo.PageResult;

import java.util.List;
import java.util.Map;

public interface ReviewService extends IService<Review> {
    // 提交评价并自动更新信用分
    boolean submitReview(Review review);

    // 检查用户是否可以对特定互助进行评价
    boolean canReview(Long userId, Long helpInfoId, String reviewType);

    // 检查互助信息的评价状态
    Map<String, Boolean> getHelpInfoReviewStatus(Long helpInfoId);

    // 获取用户的信用分
    Integer getUserCreditScore(Long userId);

    // 分页和多条件查询评价
    PageResult<ReviewVO> getReviewList(Long userId, String type, Integer score, String moduleType, Integer page,
            Integer size);

    // 获取用户对特定互助信息的评价状态
    Map<String, Object> getUserReviewStatus(Long userId, Long helpInfoId);
}
