package com.example.campusbuddy.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.campusbuddy.entity.HelpInfo;
import com.example.campusbuddy.entity.User;
import com.example.campusbuddy.exception.ResourceNotFoundException;
import com.example.campusbuddy.mapper.HelpInfoMapper;
import com.example.campusbuddy.mapper.UserMapper;
import com.example.campusbuddy.service.HelpInfoService;
import com.example.campusbuddy.vo.HelpInfoDetailVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HelpInfoServiceImpl extends ServiceImpl<HelpInfoMapper, HelpInfo> implements HelpInfoService {

    @Autowired
    private UserMapper userMapper;

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

        return vo;
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
