package com.example.campusbuddy.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.campusbuddy.entity.HelpInfo;
import com.example.campusbuddy.mapper.HelpInfoMapper;
import com.example.campusbuddy.service.HelpInfoService;
import org.springframework.stereotype.Service;

@Service
public class HelpInfoServiceImpl extends ServiceImpl<HelpInfoMapper, HelpInfo> implements HelpInfoService {
    // 可扩展自定义方法
}
