package com.example.campusbuddy.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.campusbuddy.entity.Group;
import com.example.campusbuddy.vo.GroupVO;

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
    
    // =============== 管理员方法 ===============
    // 管理员分页查询小组
    Page<Group> adminPageGroups(Integer page, Integer size, String keyword, String status);
    
    // 管理员分页查询小组（带VO，包含创建者信息）
    Page<GroupVO> adminPageGroupVOs(Integer page, Integer size, String keyword, String status);
    
    // 管理员更新小组状态
    boolean adminUpdateGroupStatus(Long groupId, String status);
    
    // 管理员删除小组
    boolean adminDeleteGroup(Long groupId);
}
