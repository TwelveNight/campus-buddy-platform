package com.example.campusbuddy.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.campusbuddy.common.R;
import com.example.campusbuddy.entity.Group;
import com.example.campusbuddy.entity.GroupMember;
import com.example.campusbuddy.entity.User;
import com.example.campusbuddy.service.GroupMemberService;
import com.example.campusbuddy.service.GroupService;
import com.example.campusbuddy.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/groups")
public class GroupController {

    @Autowired
    private GroupService groupService;

    @Autowired
    private GroupMemberService groupMemberService;

    @Autowired
    private UserService userService;

    /**
     * 获取当前认证用户
     */
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userService.getUserByUsername(username);
    }

    /**
     * 创建学习小组
     */
    @PostMapping
    public R<Long> createGroup(@RequestBody Group group) {
        User currentUser = getCurrentUser();
        group.setCreatorId(currentUser.getUserId());
        
        Long groupId = groupService.createGroup(group);
        return R.ok("小组创建成功", groupId);
    }

    /**
     * 获取小组列表（分页）
     */
    @GetMapping
    public R<IPage<Group>> getGroups(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String tag,
            @RequestParam(required = false) String keyword) {
        
        IPage<Group> groupPage = groupService.queryGroups(pageNum, pageSize, category, tag, keyword);
        return R.ok(groupPage);
    }

    /**
     * 获取小组详情
     */
    @GetMapping("/{groupId}")
    public R<Group> getGroupDetail(@PathVariable Long groupId) {
        Group group = groupService.getGroupDetail(groupId);
        if (group == null) {
            return R.fail("小组不存在");
        }
        return R.ok(group);
    }

    /**
     * 更新小组信息
     */
    @PutMapping("/{groupId}")
    public R<Void> updateGroup(@PathVariable Long groupId, @RequestBody Group group) {
        User currentUser = getCurrentUser();
        
        // 验证是否有权限更新
        Group existingGroup = groupService.getById(groupId);
        if (existingGroup == null) {
            return R.fail("小组不存在");
        }
        
        if (!existingGroup.getCreatorId().equals(currentUser.getUserId())) {
            return R.fail("只有小组创建者可以更新小组信息");
        }
        
        group.setGroupId(groupId);
        boolean success = groupService.updateGroup(group);
        
        return success ? R.ok("小组信息更新成功", null) : R.fail("小组信息更新失败");
    }

    /**
     * 解散小组
     */
    @DeleteMapping("/{groupId}")
    public R<Void> disbandGroup(@PathVariable Long groupId) {
        User currentUser = getCurrentUser();
        boolean success = groupService.disbandGroup(groupId, currentUser.getUserId());
        
        return success ? R.ok("小组已成功解散", null) : R.fail("解散小组失败，请确认您是小组创建者");
    }

    /**
     * 获取用户创建的小组
     */
    @GetMapping("/created")
    public R<List<Group>> getCreatedGroups() {
        User currentUser = getCurrentUser();
        List<Group> groups = groupService.getUserCreatedGroups(currentUser.getUserId());
        return R.ok(groups);
    }

    /**
     * 获取用户加入的小组
     */
    @GetMapping("/joined")
    public R<List<Group>> getJoinedGroups() {
        User currentUser = getCurrentUser();
        List<Group> groups = groupService.getUserJoinedGroups(currentUser.getUserId());
        return R.ok(groups);
    }

    /**
     * 加入小组
     */
    @PostMapping("/{groupId}/join")
    public R<Void> joinGroup(@PathVariable Long groupId) {
        User currentUser = getCurrentUser();
        
        // 检查小组是否存在
        Group group = groupService.getById(groupId);
        if (group == null) {
            return R.fail("小组不存在");
        }
        
        // 检查用户是否已经是小组成员
        LambdaQueryWrapper<GroupMember> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(GroupMember::getGroupId, groupId)
                .eq(GroupMember::getUserId, currentUser.getUserId());
        
        if (groupMemberService.count(queryWrapper) > 0) {
            return R.fail("您已经是小组成员");
        }
        
        // 创建小组成员记录
        GroupMember member = new GroupMember();
        member.setGroupId(groupId);
        member.setUserId(currentUser.getUserId());
        member.setRole("MEMBER");
        
        // 根据加入方式确定状态
        if ("PUBLIC".equals(group.getJoinType())) {
            member.setStatus("ACTIVE");
            // 更新小组成员数量
            group.setMemberCount(group.getMemberCount() + 1);
            groupService.updateById(group);
        } else {
            member.setStatus("PENDING_APPROVAL");
        }
        
        member.setJoinedAt(new Date());
        groupMemberService.save(member);
        
        return R.ok("PUBLIC".equals(group.getJoinType()) ? "成功加入小组" : "已提交加入申请，等待审核", null);
    }

    /**
     * 退出小组
     */
    @PostMapping("/{groupId}/quit")
    public R<Void> quitGroup(@PathVariable Long groupId) {
        User currentUser = getCurrentUser();
        
        // 检查是否是小组成员
        LambdaQueryWrapper<GroupMember> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(GroupMember::getGroupId, groupId)
                .eq(GroupMember::getUserId, currentUser.getUserId())
                .eq(GroupMember::getStatus, "ACTIVE");
        
        GroupMember member = groupMemberService.getOne(queryWrapper);
        if (member == null) {
            return R.fail("您不是小组成员");
        }
        
        // 检查是否是小组创建者
        if ("CREATOR".equals(member.getRole())) {
            return R.fail("小组创建者不能退出小组，请转让小组或解散小组");
        }
        
        // 移除成员记录
        groupMemberService.remove(queryWrapper);
        
        // 更新小组成员数量
        Group group = groupService.getById(groupId);
        if (group != null && group.getMemberCount() > 0) {
            group.setMemberCount(group.getMemberCount() - 1);
            groupService.updateById(group);
        }
        
        return R.ok("已退出小组", null);
    }

    /**
     * 审批加入申请
     */
    @PostMapping("/{groupId}/members/{userId}/approve")
    public R<Void> approveJoinRequest(@PathVariable Long groupId, @PathVariable Long userId) {
        User currentUser = getCurrentUser();
        
        // 检查当前用户是否有权限审批（创建者或管理员）
        LambdaQueryWrapper<GroupMember> adminQuery = new LambdaQueryWrapper<>();
        adminQuery.eq(GroupMember::getGroupId, groupId)
                .eq(GroupMember::getUserId, currentUser.getUserId())
                .in(GroupMember::getRole, "CREATOR", "ADMIN");
        
        if (groupMemberService.count(adminQuery) == 0) {
            return R.fail("您没有权限审批加入申请");
        }
        
        // 查找申请记录
        LambdaQueryWrapper<GroupMember> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(GroupMember::getGroupId, groupId)
                .eq(GroupMember::getUserId, userId)
                .eq(GroupMember::getStatus, "PENDING_APPROVAL");
        
        GroupMember member = groupMemberService.getOne(queryWrapper);
        if (member == null) {
            return R.fail("找不到待审批的申请");
        }
        
        // 更新状态为已激活
        member.setStatus("ACTIVE");
        groupMemberService.updateById(member);
        
        // 更新小组成员数量
        Group group = groupService.getById(groupId);
        if (group != null) {
            group.setMemberCount(group.getMemberCount() + 1);
            groupService.updateById(group);
        }
        
        return R.ok("已批准加入申请", null);
    }

    /**
     * 拒绝加入申请
     */
    @PostMapping("/{groupId}/members/{userId}/reject")
    public R<Void> rejectJoinRequest(@PathVariable Long groupId, @PathVariable Long userId) {
        User currentUser = getCurrentUser();
        
        // 检查当前用户是否有权限审批
        LambdaQueryWrapper<GroupMember> adminQuery = new LambdaQueryWrapper<>();
        adminQuery.eq(GroupMember::getGroupId, groupId)
                .eq(GroupMember::getUserId, currentUser.getUserId())
                .in(GroupMember::getRole, "CREATOR", "ADMIN");
        
        if (groupMemberService.count(adminQuery) == 0) {
            return R.fail("您没有权限审批加入申请");
        }
        
        // 查找申请记录
        LambdaQueryWrapper<GroupMember> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(GroupMember::getGroupId, groupId)
                .eq(GroupMember::getUserId, userId)
                .eq(GroupMember::getStatus, "PENDING_APPROVAL");
        
        // 删除申请记录
        groupMemberService.remove(queryWrapper);
        
        return R.ok("已拒绝加入申请", null);
    }

    /**
     * 获取小组成员列表
     */
    @GetMapping("/{groupId}/members")
    public R<List<GroupMember>> getGroupMembers(@PathVariable Long groupId) {
        LambdaQueryWrapper<GroupMember> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(GroupMember::getGroupId, groupId);
        
        List<GroupMember> members = groupMemberService.list(queryWrapper);
        return R.ok(members);
    }

    /**
     * 设置小组管理员
     */
    @PostMapping("/{groupId}/members/{userId}/set-admin")
    public R<Void> setAdmin(@PathVariable Long groupId, @PathVariable Long userId) {
        User currentUser = getCurrentUser();
        
        // 检查当前用户是否是创建者
        Group group = groupService.getById(groupId);
        if (group == null) {
            return R.fail("小组不存在");
        }
        
        if (!group.getCreatorId().equals(currentUser.getUserId())) {
            return R.fail("只有小组创建者可以设置管理员");
        }
        
        // 检查目标用户是否是小组成员
        LambdaQueryWrapper<GroupMember> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(GroupMember::getGroupId, groupId)
                .eq(GroupMember::getUserId, userId)
                .eq(GroupMember::getStatus, "ACTIVE");
        
        GroupMember member = groupMemberService.getOne(queryWrapper);
        if (member == null) {
            return R.fail("该用户不是小组成员");
        }
        
        // 设置为管理员
        member.setRole("ADMIN");
        groupMemberService.updateById(member);
        
        return R.ok("已设置为小组管理员", null);
    }

    /**
     * 取消小组管理员
     */
    @PostMapping("/{groupId}/members/{userId}/cancel-admin")
    public R<Void> cancelAdmin(@PathVariable Long groupId, @PathVariable Long userId) {
        User currentUser = getCurrentUser();
        
        // 检查当前用户是否是创建者
        Group group = groupService.getById(groupId);
        if (group == null) {
            return R.fail("小组不存在");
        }
        
        if (!group.getCreatorId().equals(currentUser.getUserId())) {
            return R.fail("只有小组创建者可以取消管理员");
        }
        
        // 检查目标用户是否是管理员
        LambdaQueryWrapper<GroupMember> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(GroupMember::getGroupId, groupId)
                .eq(GroupMember::getUserId, userId)
                .eq(GroupMember::getRole, "ADMIN");
        
        GroupMember member = groupMemberService.getOne(queryWrapper);
        if (member == null) {
            return R.fail("该用户不是小组管理员");
        }
        
        // 取消管理员角色
        member.setRole("MEMBER");
        groupMemberService.updateById(member);
        
        return R.ok("已取消小组管理员", null);
    }

    /**
     * 移除小组成员
     */
    @DeleteMapping("/{groupId}/members/{userId}")
    public R<Void> removeMember(@PathVariable Long groupId, @PathVariable Long userId) {
        User currentUser = getCurrentUser();
        
        // 检查当前用户是否有权限（创建者或管理员）
        LambdaQueryWrapper<GroupMember> adminQuery = new LambdaQueryWrapper<>();
        adminQuery.eq(GroupMember::getGroupId, groupId)
                .eq(GroupMember::getUserId, currentUser.getUserId())
                .in(GroupMember::getRole, "CREATOR", "ADMIN");
        
        if (groupMemberService.count(adminQuery) == 0) {
            return R.fail("您没有权限移除成员");
        }
        
        // 不能移除创建者
        Group group = groupService.getById(groupId);
        if (group.getCreatorId().equals(userId)) {
            return R.fail("不能移除小组创建者");
        }
        
        // 管理员不能移除其他管理员
        GroupMember currentMember = groupMemberService.getOne(adminQuery);
        if ("ADMIN".equals(currentMember.getRole())) {
            LambdaQueryWrapper<GroupMember> targetQuery = new LambdaQueryWrapper<>();
            targetQuery.eq(GroupMember::getGroupId, groupId)
                    .eq(GroupMember::getUserId, userId)
                    .eq(GroupMember::getRole, "ADMIN");
            
            if (groupMemberService.count(targetQuery) > 0) {
                return R.fail("管理员不能移除其他管理员");
            }
        }
        
        // 移除成员
        LambdaQueryWrapper<GroupMember> memberQuery = new LambdaQueryWrapper<>();
        memberQuery.eq(GroupMember::getGroupId, groupId).eq(GroupMember::getUserId, userId);
        
        boolean success = groupMemberService.remove(memberQuery);
        
        // 更新小组成员数量
        if (success) {
            if (group != null && group.getMemberCount() > 0) {
                group.setMemberCount(group.getMemberCount() - 1);
                groupService.updateById(group);
            }
        }
        
        return success ? R.ok("已移除小组成员", null) : R.fail("移除成员失败");
    }

    /**
     * 获取小组统计信息
     */
    @GetMapping("/stats")
    public R<Map<String, Object>> getGroupStats() {
        User currentUser = getCurrentUser();
        
        Map<String, Object> stats = new HashMap<>();
        
        // 获取用户创建的小组数
        LambdaQueryWrapper<Group> createdQuery = new LambdaQueryWrapper<>();
        createdQuery.eq(Group::getCreatorId, currentUser.getUserId());
        stats.put("createdCount", groupService.count(createdQuery));
        
        // 获取用户加入的小组数
        LambdaQueryWrapper<GroupMember> joinedQuery = new LambdaQueryWrapper<>();
        joinedQuery.eq(GroupMember::getUserId, currentUser.getUserId())
                .eq(GroupMember::getStatus, "ACTIVE");
        stats.put("joinedCount", groupMemberService.count(joinedQuery));
        
        // 获取小组状态数量统计
        stats.put("statusCounts", groupService.getGroupStatusCounts());
        
        return R.ok(stats);
    }
}
