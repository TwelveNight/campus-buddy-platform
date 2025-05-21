package com.example.campusbuddy.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.campusbuddy.entity.HelpApplication;
import com.example.campusbuddy.mapper.HelpApplicationMapper;
import com.example.campusbuddy.service.HelpApplicationService;
import org.springframework.stereotype.Service;

@Service
public class HelpApplicationServiceImpl extends ServiceImpl<HelpApplicationMapper, HelpApplication>
        implements HelpApplicationService {
    // 可扩展自定义方法
}
