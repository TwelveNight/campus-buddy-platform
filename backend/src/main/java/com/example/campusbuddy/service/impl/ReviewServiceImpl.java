package com.example.campusbuddy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.campusbuddy.entity.Review;
import com.example.campusbuddy.entity.User;
import com.example.campusbuddy.entity.HelpInfo;
import com.example.campusbuddy.entity.HelpApplication;
import com.example.campusbuddy.mapper.ReviewMapper;
import com.example.campusbuddy.mapper.UserMapper;
import com.example.campusbuddy.mapper.HelpInfoMapper;
import com.example.campusbuddy.mapper.HelpApplicationMapper;
import com.example.campusbuddy.service.ReviewService;
import com.example.campusbuddy.service.CreditScoreCalculationService;
import com.example.campusbuddy.service.UserCacheService;
import com.example.campusbuddy.vo.PageResult;
import com.example.campusbuddy.vo.ReviewVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class ReviewServiceImpl extends ServiceImpl<ReviewMapper, Review> implements ReviewService {
    @Autowired
    private ReviewMapper reviewMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private HelpInfoMapper helpInfoMapper;
    @Autowired
    private HelpApplicationMapper helpApplicationMapper;
    @Autowired
    private CreditScoreCalculationService creditScoreCalculationService;
    @Autowired
    private UserCacheService userCacheService;

    @Override
    @Transactional
    public boolean submitReview(Review review) {
        try {
            log.info("提交评价: {}", review);

            // 检查必要字段
            if (review.getReviewedUserId() == null) {
                log.error("提交评价失败：被评价用户ID不能为空");
                return false;
            }

            if (review.getReviewerUserId() == null) {
                log.error("提交评价失败：评价者用户ID不能为空");
                return false;
            }

            if (review.getRelatedInfoId() == null) {
                log.error("提交评价失败：关联互助任务ID不能为空");
                return false;
            }

            // 验证被评价用户是否存在
            User reviewedUser = userMapper.selectById(review.getReviewedUserId());
            if (reviewedUser == null) {
                log.error("提交评价失败：被评价用户不存在，ID: {}", review.getReviewedUserId());
                return false;
            }

            // 自动填充 moduleType 字段（如未传入）
            if (review.getModuleType() == null || review.getModuleType().isEmpty()) {
                if (review.getRelatedInfoId() != null) {
                    com.example.campusbuddy.entity.HelpInfo helpInfo = helpInfoMapper
                            .selectById(review.getRelatedInfoId());
                    if (helpInfo != null && helpInfo.getType() != null) {
                        review.setModuleType(helpInfo.getType());
                    } else {
                        review.setModuleType("HELP"); // 兜底
                    }
                } else {
                    review.setModuleType("HELP");
                }
            }

            log.info("准备插入评价记录: {}", review);
            // 保存评价
            int insertResult = reviewMapper.insert(review);
            
            // 使用新的信用积分计算服务更新用户信用分
            if (insertResult > 0 && review.getReviewedUserId() != null) {
                log.info("评价插入成功，开始更新用户信用分，被评价用户ID: {}", review.getReviewedUserId());
                
                try {
                    // 使用增量更新方式计算新的信用积分
                    Integer newCreditScore = creditScoreCalculationService.incrementalUpdateCreditScore(
                        review.getReviewedUserId(), review);
                    
                    // 更新数据库中的用户信用分
                    User user = userMapper.selectById(review.getReviewedUserId());
                    if (user != null) {
                        Integer oldScore = user.getCreditScore();
                        user.setCreditScore(newCreditScore);
                        userMapper.updateById(user);
                        
                        // 清除用户相关缓存，确保数据一致性
                        userCacheService.evictUserCache(review.getReviewedUserId());
                        
                        log.info("用户信用分更新成功，用户ID: {}，旧分数: {}，新信用分: {}",
                                user.getUserId(), oldScore, newCreditScore);
                    } else {
                        log.warn("未找到用户信息，无法更新信用分，用户ID: {}", review.getReviewedUserId());
                    }
                } catch (Exception e) {
                    log.error("更新用户信用分时发生异常，用户ID: {}", review.getReviewedUserId(), e);
                    // 不影响评价提交的成功，只记录错误
                }
                
                return true;
            }

            if (insertResult <= 0) {
                log.error("评价插入失败");
            }
            return insertResult > 0;
        } catch (Exception e) {
            log.error("提交评价过程中发生异常", e);
            return false;
        }
    }

    @Override
    public boolean canReview(Long userId, Long helpInfoId, String reviewType) {
        // 获取互助任务
        HelpInfo helpInfo = helpInfoMapper.selectById(helpInfoId);
        if (helpInfo == null) {
            return false;
        }

        // 检查互助任务状态
        boolean hasValidStatus = "RESOLVED".equals(helpInfo.getStatus()) || "UNSATISFIED".equals(helpInfo.getStatus());
        if (!hasValidStatus) {
            return false;
        }

        // 检查是否有被接受的申请
        Long acceptedAppId = helpInfo.getAcceptedApplicationId();
        if (acceptedAppId == null) {
            return false;
        }

        // 获取被接受的申请
        HelpApplication application = helpApplicationMapper.selectById(acceptedAppId);
        if (application == null) {
            return false;
        }

        // 发布者评价帮助者
        if ("PUBLISHER_TO_HELPER".equals(reviewType)) {
            // 检查当前用户是否是互助发布者
            boolean isPublisher = userId.equals(helpInfo.getPublisherId());
            if (!isPublisher) {
                return false;
            }

            // 检查是否已评价
            Long count = reviewMapper.selectCount(new QueryWrapper<Review>()
                    .eq("reviewer_user_id", userId)
                    .eq("related_info_id", helpInfoId)
                    .eq("review_type", "PUBLISHER_TO_HELPER"));
            return count == 0;
        }
        // 帮助者评价发布者
        else if ("HELPER_TO_PUBLISHER".equals(reviewType)) {
            // 检查当前用户是否是被接受的申请者
            boolean isHelper = userId.equals(application.getApplicantId());
            if (!isHelper) {
                return false;
            }

            // 检查是否已评价
            Long count = reviewMapper.selectCount(new QueryWrapper<Review>()
                    .eq("reviewer_user_id", userId)
                    .eq("related_info_id", helpInfoId)
                    .eq("review_type", "HELPER_TO_PUBLISHER"));
            return count == 0;
        }

        return false;
    }

    @Override
    public Map<String, Boolean> getHelpInfoReviewStatus(Long helpInfoId) {
        Map<String, Boolean> result = new HashMap<>();

        // 获取互助任务
        HelpInfo helpInfo = helpInfoMapper.selectById(helpInfoId);
        if (helpInfo == null) {
            result.put("publisherHasReviewed", false);
            result.put("helperHasReviewed", false);
            return result;
        }

        // 获取被接受的申请
        Long acceptedAppId = helpInfo.getAcceptedApplicationId();
        if (acceptedAppId == null) {
            result.put("publisherHasReviewed", false);
            result.put("helperHasReviewed", false);
            return result;
        }

        HelpApplication application = helpApplicationMapper.selectById(acceptedAppId);
        if (application == null) {
            result.put("publisherHasReviewed", false);
            result.put("helperHasReviewed", false);
            return result;
        }

        // 检查发布者是否已评价
        Long publisherReviewCount = reviewMapper.selectCount(new QueryWrapper<Review>()
                .eq("reviewer_user_id", helpInfo.getPublisherId())
                .eq("related_info_id", helpInfoId)
                .eq("review_type", "PUBLISHER_TO_HELPER"));
        result.put("publisherHasReviewed", publisherReviewCount > 0);

        // 检查帮助者是否已评价
        Long helperReviewCount = reviewMapper.selectCount(new QueryWrapper<Review>()
                .eq("reviewer_user_id", application.getApplicantId())
                .eq("related_info_id", helpInfoId)
                .eq("review_type", "HELPER_TO_PUBLISHER"));
        result.put("helperHasReviewed", helperReviewCount > 0);

        return result;
    }

    @Override
    public Integer getUserCreditScore(Long userId) {
        // 使用新的信用积分计算服务
        return creditScoreCalculationService.calculateCreditScore(userId);
    }

    @Override
    public PageResult<ReviewVO> getReviewList(Long userId, String type, Integer score, String moduleType, Integer page,
            Integer size) {
        log.info("Service层: 获取评价列表: userId={}, type={}, score={}, moduleType={}, page={}, size={}",
                userId, type, score, moduleType, page, size);

        // 先检查数据库中是否有自评价数据（reviewer_user_id = reviewed_user_id）
        Long selfReviewCount = reviewMapper.selectCount(
                new QueryWrapper<Review>().eq("reviewer_user_id", userId).eq("reviewed_user_id", userId));

        if (selfReviewCount > 0) {
            log.warn("警告: 数据库中存在自评价记录，用户ID={}, 数量={}", userId, selfReviewCount);
        }

        int offset = (page - 1) * size;
        List<ReviewVO> items = reviewMapper.selectReviewVOsForPage(userId, type, score, moduleType, offset, size);

        // 打印SQL查询结果
        if (items != null && !items.isEmpty()) {
            log.info("查询到{}条评价", items.size());
            ReviewVO first = items.get(0);
            log.info("第一条评价: reviewId={}, reviewerUserId={}, reviewedUserId={}, reviewType={}, reviewedNickname={}",
                    first.getReviewId(), first.getReviewerUserId(), first.getReviewedUserId(),
                    first.getReviewType(), first.getReviewedNickname());

            // 关键检查: 当type=received时，检查评价人是否正确
            if ("received".equals(type)) {
                for (ReviewVO review : items) {
                    if (review.getReviewerUserId().equals(review.getReviewedUserId())) {
                        log.error("错误: 评价人和被评价人是同一个人! reviewId={}", review.getReviewId());
                    }
                    if (!review.getReviewedUserId().equals(userId)) {
                        log.error("错误: 收到的评价中，被评价人不是当前用户! reviewId={}, reviewedUserId={}, userId={}",
                                review.getReviewId(), review.getReviewedUserId(), userId);
                    }
                }
            }
            
            // 为每条评价添加评价者的信用等级信息
            for (ReviewVO review : items) {
                try {
                    // 获取评价者的信用分数
                    Integer reviewerCreditScore = creditScoreCalculationService.calculateCreditScore(review.getReviewerUserId());
                    // 获取评价者的信用等级
                    String reviewerCreditLevel = creditScoreCalculationService.getCreditLevel(reviewerCreditScore);
                    
                    // 设置到评价VO对象中
                    review.setReviewerCreditScore(reviewerCreditScore);
                    review.setReviewerCreditLevel(reviewerCreditLevel);
                    
                    log.debug("添加评价者信用信息: reviewId={}, reviewerUserId={}, creditScore={}, creditLevel={}",
                            review.getReviewId(), review.getReviewerUserId(), reviewerCreditScore, reviewerCreditLevel);
                } catch (Exception e) {
                    log.warn("获取评价者信用信息失败: reviewId={}, reviewerUserId={}", 
                            review.getReviewId(), review.getReviewerUserId(), e);
                }
            }
        } else {
            log.info("没有查询到评价记录");
        }

        long total = reviewMapper.countReviewVOsForPage(userId, type, score, moduleType);
        return new PageResult<>(items, total, page, size);
    }

    @Override
    public Map<String, Object> getUserReviewStatus(Long userId, Long helpInfoId) {
        log.info("获取用户评价状态: userId={}, helpInfoId={}", userId, helpInfoId);
        Map<String, Object> result = new HashMap<>();

        // 获取互助任务
        HelpInfo helpInfo = helpInfoMapper.selectById(helpInfoId);
        if (helpInfo == null) {
            log.warn("互助任务不存在: helpInfoId={}", helpInfoId);
            result.put("canPublisherReview", false);
            result.put("canHelperReview", false);
            result.put("publisherHasReviewed", false);
            result.put("helperHasReviewed", false);
            return result;
        }

        // 获取被接受的申请
        Long acceptedAppId = helpInfo.getAcceptedApplicationId();
        if (acceptedAppId == null) {
            log.warn("互助任务没有被接受的申请: helpInfoId={}", helpInfoId);
            result.put("canPublisherReview", false);
            result.put("canHelperReview", false);
            result.put("publisherHasReviewed", false);
            result.put("helperHasReviewed", false);
            return result;
        }

        // 获取申请信息
        HelpApplication application = helpApplicationMapper.selectById(acceptedAppId);
        if (application == null) {
            log.warn("申请信息不存在: acceptedAppId={}", acceptedAppId);
            result.put("canPublisherReview", false);
            result.put("canHelperReview", false);
            result.put("publisherHasReviewed", false);
            result.put("helperHasReviewed", false);
            return result;
        }

        // 检查互助任务状态
        boolean hasValidStatus = "RESOLVED".equals(helpInfo.getStatus()) || "UNSATISFIED".equals(helpInfo.getStatus());
        boolean isPublisher = userId.equals(helpInfo.getPublisherId());
        boolean isHelper = userId.equals(application.getApplicantId());

        // 检查发布者是否已评价
        Long publisherReviewCount = reviewMapper.selectCount(new QueryWrapper<Review>()
                .eq("reviewer_user_id", helpInfo.getPublisherId())
                .eq("related_info_id", helpInfoId)
                .eq("review_type", "PUBLISHER_TO_HELPER"));
        boolean publisherHasReviewed = publisherReviewCount > 0;

        // 检查帮助者是否已评价
        Long helperReviewCount = reviewMapper.selectCount(new QueryWrapper<Review>()
                .eq("reviewer_user_id", application.getApplicantId())
                .eq("related_info_id", helpInfoId)
                .eq("review_type", "HELPER_TO_PUBLISHER"));
        boolean helperHasReviewed = helperReviewCount > 0;

        // 设置结果
        result.put("canPublisherReview", hasValidStatus && isPublisher && !publisherHasReviewed);
        result.put("canHelperReview", hasValidStatus && isHelper && !helperHasReviewed);
        result.put("publisherHasReviewed", publisherHasReviewed);
        result.put("helperHasReviewed", helperHasReviewed);
        result.put("isPublisher", isPublisher);
        result.put("isHelper", isHelper);

        return result;
    }
}
