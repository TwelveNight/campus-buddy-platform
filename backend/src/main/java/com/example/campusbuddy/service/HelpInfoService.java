package com.example.campusbuddy.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.campusbuddy.entity.HelpInfo;
import com.example.campusbuddy.vo.HelpInfoDetailVO;
import com.example.campusbuddy.vo.HelpInfoVO;

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

    /**
     * 管理员分页查询互助任务
     * 
     * @param page    页码
     * @param size    每页大小
     * @param keyword 关键词
     * @param type    类型
     * @param status  状态
     * @return 分页结果
     */
    Page<HelpInfoVO> adminPageHelpInfo(Integer page, Integer size, String keyword, String type, String status);

    /**
     * 管理员更新互助任务状态
     * 
     * @param id     互助任务ID
     * @param status 新状态
     * @return 是否成功
     */
    boolean adminUpdateStatus(Long id, String status);

    /**
     * 带缓存的分页查询互助任务列表
     * 
     * @param page 分页参数
     * @param queryWrapper 查询条件
     * @param type 任务类型
     * @param status 任务状态
     * @param publisherId 发布者ID
     * @param keyword 关键词
     * @return 分页查询结果
     */
    Page<HelpInfo> pageWithCache(Page<HelpInfo> page, QueryWrapper<HelpInfo> queryWrapper, String type, String status, String publisherId, String keyword);
}
