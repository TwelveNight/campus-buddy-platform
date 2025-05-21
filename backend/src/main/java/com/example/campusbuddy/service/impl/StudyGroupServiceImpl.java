package com.example.campusbuddy.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.campusbuddy.entity.StudyGroup;
import com.example.campusbuddy.mapper.StudyGroupMapper;
import com.example.campusbuddy.service.StudyGroupService;
import org.springframework.stereotype.Service;

@Service
public class StudyGroupServiceImpl extends ServiceImpl<StudyGroupMapper, StudyGroup> implements StudyGroupService {
    // 可扩展自定义方法
}
