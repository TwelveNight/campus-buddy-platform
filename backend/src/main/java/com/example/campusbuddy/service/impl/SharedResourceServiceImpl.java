package com.example.campusbuddy.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.campusbuddy.entity.SharedResource;
import com.example.campusbuddy.mapper.SharedResourceMapper;
import com.example.campusbuddy.service.SharedResourceService;
import org.springframework.stereotype.Service;

@Service
public class SharedResourceServiceImpl extends ServiceImpl<SharedResourceMapper, SharedResource>
        implements SharedResourceService {
    // 可扩展自定义方法
}
