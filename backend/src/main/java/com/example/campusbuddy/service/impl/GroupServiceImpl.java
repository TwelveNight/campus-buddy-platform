package com.example.campusbuddy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.campusbuddy.entity.Group;
import com.example.campusbuddy.entity.GroupMember;
import com.example.campusbuddy.mapper.GroupMapper;
import com.example.campusbuddy.mapper.GroupMemberMapper;
import com.example.campusbuddy.service.GroupService;
import com.example.campusbuddy.vo.GroupVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;

@Service
public class GroupServiceImpl extends ServiceImpl<GroupMapper, Group> implements GroupService {

    @Autowired
    private GroupMemberMapper groupMemberMapper;
    @Autowired
    private com.example.campusbuddy.mapper.UserMapper userMapper;

    @Override
    public IPage<Group> queryGroups(Integer pageNum, Integer pageSize, String category, String tag, String keyword) {
        LambdaQueryWrapper<Group> queryWrapper = new LambdaQueryWrapper<>();
        
        // 添加分类过滤条件
        if (StringUtils.hasText(category)) {
            queryWrapper.eq(Group::getCategory, category);
        }
        
        // 添加标签过滤条件
        if (StringUtils.hasText(tag)) {
            queryWrapper.like(Group::getTags, tag);
        }
        
        // 添加关键词搜索条件
        if (StringUtils.hasText(keyword)) {
            queryWrapper.like(Group::getName, keyword)
                    .or()
                    .like(Group::getDescription, keyword);
        }
        
        // 只查询活跃状态的小组
        queryWrapper.eq(Group::getStatus, "ACTIVE");
        
        // 按创建时间倒序排序
        queryWrapper.orderByDesc(Group::getCreatedAt);
        
        // 执行分页查询
        return page(new Page<>(pageNum, pageSize), queryWrapper);
    }

    @Override
    @Transactional
    public Long createGroup(Group group) {
        // 设置初始值
        group.setMemberCount(1);
        group.setPostCount(0);
        group.setStatus("ACTIVE");
        group.setCreatedAt(new Date());
        group.setUpdatedAt(new Date());
        
        // 保存小组信息
        save(group);
        
        // 创建者自动成为小组成员（创建者角色）
        GroupMember member = new GroupMember();
        member.setGroupId(group.getGroupId());
        member.setUserId(group.getCreatorId());
        member.setRole("CREATOR");
        member.setStatus("ACTIVE");
        member.setJoinedAt(new Date());
        groupMemberMapper.insert(member);
        
        return group.getGroupId();
    }

    @Override
    public boolean updateGroup(Group group) {
        group.setUpdatedAt(new Date());
        return updateById(group);
    }

    @Override
    @Transactional
    public boolean disbandGroup(Long groupId, Long userId) {
        // 验证请求用户是否是小组创建者
        LambdaQueryWrapper<Group> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Group::getGroupId, groupId).eq(Group::getCreatorId, userId);
        
        if (count(queryWrapper) == 0) {
            return false; // 不是创建者，无权解散小组
        }
        
        // 更新小组状态为已解散
        Group group = new Group();
        group.setGroupId(groupId);
        group.setStatus("DISBANDED");
        group.setUpdatedAt(new Date());
        
        return updateById(group);
    }

    @Override
    public Group getGroupDetail(Long groupId) {
        return getById(groupId);
    }

    @Override
    public List<Group> getUserCreatedGroups(Long userId) {
        LambdaQueryWrapper<Group> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Group::getCreatorId, userId);
        return list(queryWrapper);
    }

    @Override
    public List<Group> getUserJoinedGroups(Long userId) {
        // 查询用户加入的小组ID列表
        LambdaQueryWrapper<GroupMember> memberQueryWrapper = new LambdaQueryWrapper<>();
        memberQueryWrapper.eq(GroupMember::getUserId, userId).eq(GroupMember::getStatus, "ACTIVE");
        List<GroupMember> memberList = groupMemberMapper.selectList(memberQueryWrapper);
        
        // 如果没有加入任何小组，返回空列表
        if (memberList.isEmpty()) {
            return Collections.emptyList();
        }
        
        // 提取小组ID列表
        List<Long> groupIds = new ArrayList<>();
        for (GroupMember member : memberList) {
            groupIds.add(member.getGroupId());
        }
        
        // 查询小组信息
        LambdaQueryWrapper<Group> groupQueryWrapper = new LambdaQueryWrapper<>();
        groupQueryWrapper.in(Group::getGroupId, groupIds);
        return list(groupQueryWrapper);
    }

    @Override
    public Map<String, Integer> getGroupStatusCounts() {
        // 统计不同状态的小组数量
        Map<String, Integer> countMap = new HashMap<>();
        
        LambdaQueryWrapper<Group> activeQuery = new LambdaQueryWrapper<>();
        activeQuery.eq(Group::getStatus, "ACTIVE");
        countMap.put("active", Math.toIntExact(count(activeQuery)));
        
        LambdaQueryWrapper<Group> inactiveQuery = new LambdaQueryWrapper<>();
        inactiveQuery.eq(Group::getStatus, "INACTIVE");
        countMap.put("inactive", Math.toIntExact(count(inactiveQuery)));
        
        LambdaQueryWrapper<Group> disbandedQuery = new LambdaQueryWrapper<>();
        disbandedQuery.eq(Group::getStatus, "DISBANDED");
        countMap.put("disbanded", Math.toIntExact(count(disbandedQuery)));
        
        return countMap;
    }
    
    // =============== 管理员方法实现 ===============
    @Override
    public Page<Group> adminPageGroups(Integer page, Integer size, String keyword, String status) {
        Page<Group> pageInfo = new Page<>(page, size);
        LambdaQueryWrapper<Group> queryWrapper = new LambdaQueryWrapper<>();
        
        // 添加关键词搜索条件
        if (StringUtils.hasText(keyword)) {
            queryWrapper.like(Group::getName, keyword)
                    .or()
                    .like(Group::getDescription, keyword);
        }
        
        // 添加状态过滤条件
        if (StringUtils.hasText(status)) {
            queryWrapper.eq(Group::getStatus, status);
        }
        
        // 按创建时间倒序排序
        queryWrapper.orderByDesc(Group::getCreatedAt);
        
        return page(pageInfo, queryWrapper);
    }
    
    @Override
    @Transactional
    public boolean adminUpdateGroupStatus(Long groupId, String status) {
        Group group = getById(groupId);
        if (group == null) {
            return false;
        }
        
        group.setStatus(status);
        group.setUpdatedAt(new Date());
        
        return updateById(group);
    }
    
    @Override
    @Transactional
    public boolean adminDeleteGroup(Long groupId) {
        // 先删除小组成员关系
        LambdaQueryWrapper<GroupMember> memberQuery = new LambdaQueryWrapper<>();
        memberQuery.eq(GroupMember::getGroupId, groupId);
        groupMemberMapper.delete(memberQuery);
        
        // 删除小组
        return removeById(groupId);
    }

    // 管理员分页查询小组（带VO）
    @Override
    public Page<GroupVO> adminPageGroupVOs(Integer page, Integer size, String keyword, String status) {
        Page<Group> pageInfo = new Page<>(page, size);
        LambdaQueryWrapper<Group> queryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            queryWrapper.like(Group::getName, keyword)
                    .or()
                    .like(Group::getDescription, keyword);
        }
        if (StringUtils.hasText(status)) {
            queryWrapper.eq(Group::getStatus, status);
        }
        queryWrapper.orderByDesc(Group::getCreatedAt);
        Page<Group> groupPage = page(pageInfo, queryWrapper);
        List<Group> groupList = groupPage.getRecords();
        
        // 批量查创建者
        List<Long> creatorIds = new ArrayList<>();
        for (Group g : groupList) {
            if (g.getCreatorId() != null) creatorIds.add(g.getCreatorId());
        }
        Map<Long, com.example.campusbuddy.entity.User> creatorMap = new HashMap<>();
        if (!creatorIds.isEmpty()) {
            for (com.example.campusbuddy.entity.User u : userMapper.selectBatchIds(creatorIds)) {
                creatorMap.put(u.getUserId(), u);
            }
        }
        
        // 组装VO
        List<GroupVO> voList = new ArrayList<>();
        for (Group g : groupList) {
            GroupVO vo = new GroupVO();
            vo.setGroupId(g.getGroupId());
            vo.setName(g.getName());
            vo.setDescription(g.getDescription());
            vo.setAvatarUrl(g.getAvatarUrl());
            vo.setCreatorId(g.getCreatorId());
            com.example.campusbuddy.entity.User creator = creatorMap.get(g.getCreatorId());
            if (creator != null) {
                vo.setCreatorNickname(creator.getNickname() != null ? creator.getNickname() : creator.getUsername());
                vo.setCreatorAvatar(creator.getAvatarUrl());
            }
            vo.setJoinType(g.getJoinType());
            vo.setCategory(g.getCategory());
            vo.setTags(g.getTags());
            vo.setMemberCount(g.getMemberCount());
            vo.setPostCount(g.getPostCount());
            vo.setStatus(g.getStatus());
            vo.setCreatedAt(g.getCreatedAt());
            vo.setUpdatedAt(g.getUpdatedAt());
            voList.add(vo);
        }
        Page<GroupVO> voPage = new Page<>(groupPage.getCurrent(), groupPage.getSize(), groupPage.getTotal());
        voPage.setRecords(voList);
        return voPage;
    }
}
