package com.example.campusbuddy.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.campusbuddy.entity.HelpInfo;
import com.example.campusbuddy.entity.User;
import com.example.campusbuddy.entity.HelpApplication;
import com.example.campusbuddy.exception.ResourceNotFoundException;
import com.example.campusbuddy.mapper.HelpInfoMapper;
import com.example.campusbuddy.mapper.UserMapper;
import com.example.campusbuddy.mapper.HelpApplicationMapper;
import com.example.campusbuddy.service.HelpInfoService;
import com.example.campusbuddy.service.ReviewService;
import com.example.campusbuddy.vo.HelpInfoDetailVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Map;

@Service
public class HelpInfoServiceImpl extends ServiceImpl<HelpInfoMapper, HelpInfo> implements HelpInfoService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private HelpApplicationMapper helpApplicationMapper;

    @Autowired
    private ReviewService reviewService;

    @Override
    public HelpInfoDetailVO getHelpInfoDetail(Long infoId) {
        // 获取互助信息
        HelpInfo helpInfo = this.getById(infoId);
        if (helpInfo == null) {
            throw new ResourceNotFoundException("互助信息", infoId);
        }

        // 转换为VO
        HelpInfoDetailVO vo = HelpInfoDetailVO.fromEntity(helpInfo);

        // 获取发布者信息
        User publisher = userMapper.selectById(helpInfo.getPublisherId());
        if (publisher != null) {
            vo.setPublisherName(publisher.getNickname());
            vo.setPublisherAvatar(publisher.getAvatarUrl());
        }

        // 新增：如果有已接受的申请，查出帮助者昵称
        Long acceptedAppId = helpInfo.getAcceptedApplicationId();
        if (acceptedAppId != null) {
            String nickname = helpApplicationMapper.getApplicantNicknameByApplicationId(acceptedAppId);
            vo.setAcceptedApplicantNickname(nickname);
        }

        return vo;
    }

    @Override
    public HelpInfoDetailVO getHelpInfoDetail(Long infoId, Long currentUserId) {
        // 首先获取基本信息
        HelpInfoDetailVO vo = getHelpInfoDetail(infoId);

        // 获取互助信息
        HelpInfo helpInfo = this.getById(infoId);
        if (helpInfo == null) {
            throw new ResourceNotFoundException("互助信息", infoId);
        }

        // 检查是否有被接受的申请
        Long acceptedAppId = helpInfo.getAcceptedApplicationId();
        if (acceptedAppId != null) {
            // 新增：补充帮助者昵称
            String nickname = helpApplicationMapper.getApplicantNicknameByApplicationId(acceptedAppId);
            vo.setAcceptedApplicantNickname(nickname);

            HelpApplication application = helpApplicationMapper.selectById(acceptedAppId);
            if (application != null) {
                vo.setHelperId(application.getApplicantId());

                // 获取帮助者姓名
                User helper = userMapper.selectById(application.getApplicantId());
                if (helper != null) {
                    vo.setHelperName(helper.getNickname());
                }

                // 获取互助信息的评价状态
                Map<String, Boolean> reviewStatus = reviewService.getHelpInfoReviewStatus(infoId);
                vo.setPublisherHasReviewed(reviewStatus.get("publisherHasReviewed"));
                vo.setHelperHasReviewed(reviewStatus.get("helperHasReviewed"));

                // 检查当前用户是否可以评价
                if (currentUserId != null) {
                    // 当前用户是发布者且未评价
                    if (currentUserId.equals(helpInfo.getPublisherId())) {
                        vo.setCanPublisherReview(
                                reviewService.canReview(currentUserId, infoId, "PUBLISHER_TO_HELPER"));
                        vo.setCanHelperReview(false); // 不是帮助者，不能评价发布者
                    }
                    // 当前用户是帮助者且未评价
                    else if (currentUserId.equals(application.getApplicantId())) {
                        vo.setCanHelperReview(
                                reviewService.canReview(currentUserId, infoId, "HELPER_TO_PUBLISHER"));
                        vo.setCanPublisherReview(false); // 不是发布者，不能评价帮助者
                    } else {
                        // 既不是发布者也不是帮助者
                        vo.setCanPublisherReview(false);
                        vo.setCanHelperReview(false);
                    }
                } else {
                    // 未登录或没有当前用户ID
                    vo.setCanPublisherReview(false);
                    vo.setCanHelperReview(false);
                }
            } else {
                // 申请不存在，无法评价
                setDefaultReviewStatus(vo);
            }
        } else {
            // 没有被接受的申请，无法评价
            setDefaultReviewStatus(vo);
        }

        return vo;
    }

    // 辅助方法：设置默认评价状态
    private void setDefaultReviewStatus(HelpInfoDetailVO vo) {
        vo.setHelperId(null);
        vo.setHelperName(null);
        vo.setCanPublisherReview(false);
        vo.setCanHelperReview(false);
        vo.setPublisherHasReviewed(false);
        vo.setHelperHasReviewed(false);
    }

    @Override
    @Transactional
    public HelpInfo incrementViewCount(Long infoId) {
        HelpInfo helpInfo = this.getById(infoId);
        if (helpInfo == null) {
            throw new ResourceNotFoundException("互助信息", infoId);
        }

        // 增加浏览量
        helpInfo.setViewCount(helpInfo.getViewCount() + 1);
        this.updateById(helpInfo);

        return helpInfo;
    }
}
