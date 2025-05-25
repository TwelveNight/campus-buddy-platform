package com.example.campusbuddy.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.campusbuddy.entity.HelpInfo;
import com.example.campusbuddy.vo.HelpInfoDetailVO;

public interface HelpInfoService extends IService<HelpInfo> {
    /**
     * 获取互助任务详情，包含发布者信息
     * 
     * @param infoId 互助任务ID
     * @return 互助任务详情VO
     */
    HelpInfoDetailVO getHelpInfoDetail(Long infoId);

    /**
     * 获取互助任务详情，包含发布者信息和用户评价状态
     * 
     * @param infoId        互助任务ID
     * @param currentUserId 当前用户ID
     * @return 互助任务详情VO
     */
    HelpInfoDetailVO getHelpInfoDetail(Long infoId, Long currentUserId);

    /**
     * 增加互助任务浏览量
     * 
     * @param infoId 互助任务ID
     * @return 增加浏览量后的互助任务
     */
    HelpInfo incrementViewCount(Long infoId);
}
