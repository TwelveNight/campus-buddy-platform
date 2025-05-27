package com.example.campusbuddy.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.campusbuddy.entity.HelpInfo;
import com.example.campusbuddy.entity.User;
import com.example.campusbuddy.entity.HelpApplication;
import com.example.campusbuddy.exception.ResourceNotFoundException;
import com.example.campusbuddy.mapper.HelpInfoMapper;
import com.example.campusbuddy.mapper.UserMapper;
import com.example.campusbuddy.mapper.HelpApplicationMapper;
import com.example.campusbuddy.service.HelpInfoService;
import com.example.campusbuddy.service.HelpInfoCacheService;
import com.example.campusbuddy.service.ReviewService;
import com.example.campusbuddy.vo.HelpInfoDetailVO;
import com.example.campusbuddy.vo.HelpInfoVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class HelpInfoServiceImpl extends ServiceImpl<HelpInfoMapper, HelpInfo> implements HelpInfoService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private HelpApplicationMapper helpApplicationMapper;

    @Autowired
    private ReviewService reviewService;
    
    @Autowired
    private HelpInfoCacheService helpInfoCacheService;

    @Override
    public HelpInfoDetailVO getHelpInfoDetail(Long infoId) {
        // 先尝试从缓存获取
        HelpInfoDetailVO cachedVo = helpInfoCacheService.getCachedHelpInfoDetail(infoId);
        if (cachedVo != null) {
            log.info("从缓存获取互助信息详情成功, id: " + infoId);
            return cachedVo;
        }

        // 获取互助任务
        HelpInfo helpInfo = this.getById(infoId);
        if (helpInfo == null) {
            throw new ResourceNotFoundException("互助任务", infoId);
        }

        // 转换为VO
        HelpInfoDetailVO vo = HelpInfoDetailVO.fromEntity(helpInfo);

        // 获取发布者信息 - 先从缓存查找
        User publisher = helpInfoCacheService.getCachedUser(helpInfo.getPublisherId());
        if (publisher == null) {
            publisher = userMapper.selectById(helpInfo.getPublisherId());
            if (publisher != null) {
                // 缓存用户信息
                helpInfoCacheService.cacheUser(helpInfo.getPublisherId(), publisher, 3600); // 1小时
            }
        }
        
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

        // 缓存详情信息
        helpInfoCacheService.cacheHelpInfoDetail(infoId, vo, 1800); // 30分钟

        return vo;
    }

    @Override
    public HelpInfoDetailVO getHelpInfoDetail(Long infoId, Long currentUserId) {
        // 首先获取基本信息
        HelpInfoDetailVO vo = getHelpInfoDetail(infoId);

        // 获取互助任务
        HelpInfo helpInfo = this.getById(infoId);
        if (helpInfo == null) {
            throw new ResourceNotFoundException("互助任务", infoId);
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

                // 获取互助任务的评价状态
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
            throw new ResourceNotFoundException("互助任务", infoId);
        }

        // 使用缓存服务增加浏览量，延迟批量更新
        helpInfoCacheService.incrementViewCount(infoId);
        
        // 为了保持API兼容性，直接返回当前互助信息对象
        // 注意：此时返回的浏览量可能不是最新的，因为实际更新会在批处理中进行
        return helpInfo;
    }

    @Override
    public Page<HelpInfoVO> adminPageHelpInfo(Integer page, Integer size, String keyword, String type, String status) {
        // 生成缓存键
        String cacheKey = helpInfoCacheService.generateAdminCacheKey(page, size, keyword, type, status);
        
        // 先尝试从缓存获取
        Page<HelpInfoVO> cachedResult = helpInfoCacheService.getCachedAdminHelpInfoList(cacheKey);
        if (cachedResult != null) {
            log.debug("从缓存获取管理员查询结果成功, key: {}", cacheKey);
            return cachedResult;
        }
        
        // 缓存未命中，从数据库查询
        Page<HelpInfo> helpInfoPage = new Page<>(page, size);
        QueryWrapper<HelpInfo> queryWrapper = new QueryWrapper<>();
        
        // 关键词搜索
        if (keyword != null && !keyword.trim().isEmpty()) {
            queryWrapper.and(wrapper -> wrapper
                .like("title", keyword)
                .or()
                .like("description", keyword)
            );
        }
        
        // 类型过滤
        if (type != null && !type.trim().isEmpty()) {
            queryWrapper.eq("type", type);
        }
        
        // 状态过滤
        if (status != null && !status.trim().isEmpty()) {
            queryWrapper.eq("status", status);
        }
        
        queryWrapper.orderByDesc("created_at");
        
        Page<HelpInfo> result = this.page(helpInfoPage, queryWrapper);
        
        // 转换为VO
        Page<HelpInfoVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        List<HelpInfoVO> voList = result.getRecords().stream().map(helpInfo -> {
            HelpInfoVO vo = new HelpInfoVO();
            vo.setInfoId(helpInfo.getInfoId());
            vo.setTitle(helpInfo.getTitle());
            vo.setDescription(helpInfo.getDescription());
            vo.setType(helpInfo.getType());
            vo.setStatus(helpInfo.getStatus());
            vo.setReward(helpInfo.getRewardAmount() != null ? helpInfo.getRewardAmount().toString() : null);
            vo.setLocation(helpInfo.getExpectedLocation());
            vo.setDeadline(helpInfo.getCreatedAt().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime());
            vo.setCreatedAt(helpInfo.getCreatedAt().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime());
            vo.setUpdatedAt(helpInfo.getUpdatedAt().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime());
            vo.setPublisherId(helpInfo.getPublisherId());
            vo.setViewCount(helpInfo.getViewCount());
            
            // 获取发布者信息
            User publisher = userMapper.selectById(helpInfo.getPublisherId());
            if (publisher != null) {
                vo.setPublisherName(publisher.getNickname());
                vo.setPublisherAvatar(publisher.getAvatarUrl());
            }
            
            // 获取申请数量
            QueryWrapper<HelpApplication> appQueryWrapper = new QueryWrapper<>();
            appQueryWrapper.eq("info_id", helpInfo.getInfoId());
            long applicationCount = helpApplicationMapper.selectCount(appQueryWrapper);
            vo.setApplicationCount((int) applicationCount);
            
            return vo;
        }).collect(Collectors.toList());
        
        voPage.setRecords(voList);
        
        // 将结果存入缓存
        helpInfoCacheService.cacheAdminHelpInfoList(cacheKey, voPage, 1800); // 30分钟
        
        return voPage;
    }
    
    @Override
    public boolean adminUpdateStatus(Long id, String status) {
        HelpInfo helpInfo = this.getById(id);
        if (helpInfo == null) {
            return false;
        }
        
        helpInfo.setStatus(status);
        return this.updateById(helpInfo);
    }

    @Override
    public Page<HelpInfo> pageWithCache(Page<HelpInfo> page, QueryWrapper<HelpInfo> queryWrapper, String type, String status, String publisherId, String keyword) {
        // 生成缓存键
        String cacheKey = helpInfoCacheService.generateListCacheKey(
                page.getCurrent(), 
                page.getSize(),
                type,
                status,
                publisherId,
                keyword
        );
        
        // 先尝试从缓存获取
        Page<HelpInfo> cachedResult = helpInfoCacheService.getCachedHelpInfoList(cacheKey);
        if (cachedResult != null) {
            log.info("成功从缓存获取互助信息列表, 缓存键: " + cacheKey);
            return cachedResult;
        }
        
        // 缓存未命中，从数据库查询
        log.info("缓存未命中，从数据库查询互助信息列表");
        Page<HelpInfo> result = super.page(page, queryWrapper);
        
        // 缓存结果
        helpInfoCacheService.cacheHelpInfoList(cacheKey, result, 600); // 缓存10分钟
        log.info("互助信息列表已存入缓存, 缓存键: " + cacheKey);
        
        return result;
    }
}
