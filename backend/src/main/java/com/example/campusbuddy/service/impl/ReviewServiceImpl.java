package com.example.campusbuddy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.campusbuddy.entity.Review;
import com.example.campusbuddy.entity.User;
import com.example.campusbuddy.mapper.ReviewMapper;
import com.example.campusbuddy.mapper.UserMapper;
import com.example.campusbuddy.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ReviewServiceImpl extends ServiceImpl<ReviewMapper, Review> implements ReviewService {
    @Autowired
    private ReviewMapper reviewMapper;
    @Autowired
    private UserMapper userMapper;

    @Override
    @Transactional
    public boolean submitReview(Review review) {
        // 保存评价
        int insertResult = reviewMapper.insert(review);
        // 自动更新被评价用户的信用分（简单示例：平均分，实际可根据业务调整）
        if (insertResult > 0 && review.getReviewedUserId() != null) {
            List<Review> userReviews = reviewMapper.selectList(
                new QueryWrapper<Review>().eq("reviewed_user_id", review.getReviewedUserId())
            );
            int avgScore = userReviews.stream().mapToInt(Review::getScore).sum() / userReviews.size();
            User user = userMapper.selectById(review.getReviewedUserId());
            if (user != null) {
                user.setCreditScore(avgScore);
                userMapper.updateById(user);
            }
        }
        return insertResult > 0;
    }

    @Override
    public List<Review> getReviewsByHelpId(Long helpId) {
        return reviewMapper.selectList(new QueryWrapper<Review>().eq("related_info_id", helpId));
    }

    @Override
    public List<Review> getReviewsByUserId(Long userId) {
        return reviewMapper.selectList(new QueryWrapper<Review>().eq("reviewed_user_id", userId));
    }

    @Override
    public boolean hasReviewed(Long reviewerUserId, Long helpId) {
        // 检查是否已有该用户对该互助的评价记录
        Long count = reviewMapper.selectCount(new QueryWrapper<Review>()
                .eq("reviewer_user_id", reviewerUserId)
                .eq("related_info_id", helpId));
        return count > 0;
    }

    @Override
    public Integer getUserCreditScore(Long userId) {
        // 从用户表获取信用分
        User user = userMapper.selectById(userId);
        return user != null ? user.getCreditScore() : null;
    }
}
