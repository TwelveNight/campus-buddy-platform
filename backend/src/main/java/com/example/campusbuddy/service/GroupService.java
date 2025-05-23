package com.example.campusbuddy.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.campusbuddy.entity.Group;

import java.util.List;
import java.util.Map;

public interface GroupService extends IService<Group> {
    // 根据分类和标签查询小组
    IPage<Group> queryGroups(Integer pageNum, Integer pageSize, String category, String tag, String keyword);
    
    // 创建小组
    Long createGroup(Group group);
    
    // 更新小组信息
    boolean updateGroup(Group group);
    
    // 解散小组
    boolean disbandGroup(Long groupId, Long userId);
    
    // 获取小组详情
    Group getGroupDetail(Long groupId);
    
    // 获取用户创建的小组
    List<Group> getUserCreatedGroups(Long userId);
    
    // 获取用户加入的小组
    List<Group> getUserJoinedGroups(Long userId);
    
    // 统计小组各种状态的数量
    Map<String, Integer> getGroupStatusCounts();
}
