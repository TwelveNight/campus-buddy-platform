package com.example.campusbuddy.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.campusbuddy.common.R;
import com.example.campusbuddy.service.GroupService;
import com.example.campusbuddy.service.NotificationService;
import com.example.campusbuddy.dto.NotificationCreateDTO;
import com.example.campusbuddy.vo.GroupVO;
import com.example.campusbuddy.entity.Group;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "管理员小组管理", description = "管理员小组管理相关操作")
@RestController
@RequestMapping("/api/admin/group")
public class AdminGroupController {
    
    @Autowired
    private GroupService groupService;

    @Autowired
    private NotificationService notificationService;

    @Operation(summary = "分页查询小组（支持关键词和状态）")
    @GetMapping("/page")
    public R<Page<GroupVO>> pageGroups(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            HttpServletRequest request) {
        // 验证管理员权限
        @SuppressWarnings("unchecked")
        List<String> roles = (List<String>) request.getAttribute("roles");
        boolean isAdmin = roles != null && roles.contains("ROLE_ADMIN");
        
        if (!isAdmin) {
            return R.fail("权限不足，需要管理员权限");
        }
        
        Page<GroupVO> result = groupService.adminPageGroupVOs(page, size, keyword, status);
        return R.ok("查询成功", result);
    }

    @Operation(summary = "禁用/启用小组")
    @PostMapping("/{groupId}/status")
    public R<Void> updateGroupStatus(
            @PathVariable Long groupId, 
            @RequestParam String status,
            HttpServletRequest request) {
        // 验证管理员权限
        @SuppressWarnings("unchecked")
        List<String> roles = (List<String>) request.getAttribute("roles");
        boolean isAdmin = roles != null && roles.contains("ROLE_ADMIN");
        
        if (!isAdmin) {
            return R.fail("权限不足，需要管理员权限");
        }
        
        boolean ok = groupService.adminUpdateGroupStatus(groupId, status);
        if (ok) {
            // 获取小组，通知创建者
            Group group = groupService.getGroupDetail(groupId);
            if (group != null && group.getCreatorId() != null) {
                NotificationCreateDTO dto = new NotificationCreateDTO();
                dto.setRecipientId(group.getCreatorId());
                dto.setType("GROUP_STATUS");
                dto.setTitle("小组状态变更");
                String statusText = "ACTIVE".equals(status) ? "启用" : "禁用";
                dto.setContent("您的小组(ID:" + groupId + ")已被管理员" + statusText + "。");
                dto.setRelatedId(groupId);
                notificationService.createUserNotification(dto);
            }
        }
        return ok ? R.ok("操作成功", null) : R.fail("操作失败");
    }

    @Operation(summary = "管理员删除小组")
    @DeleteMapping("/{groupId}")
    public R<Void> deleteGroup(
            @PathVariable Long groupId,
            HttpServletRequest request) {
        // 验证管理员权限
        @SuppressWarnings("unchecked")
        List<String> roles = (List<String>) request.getAttribute("roles");
        boolean isAdmin = roles != null && roles.contains("ROLE_ADMIN");
        if (!isAdmin) {
            return R.fail("权限不足，需要管理员权限");
        }
        // 获取小组，通知创建者
        com.example.campusbuddy.entity.Group group = groupService.getGroupDetail(groupId);
        Long creatorId = group != null ? group.getCreatorId() : null;
        boolean ok = groupService.adminDeleteGroup(groupId);
        if (ok && creatorId != null) {
            NotificationCreateDTO dto = new NotificationCreateDTO();
            dto.setRecipientId(creatorId);
            dto.setType("GROUP_DELETE");
            dto.setTitle("小组被删除");
            dto.setContent("您的小组(ID:" + groupId + ")已被管理员删除。");
            dto.setRelatedId(groupId);
            notificationService.createUserNotification(dto);
        }
        return ok ? R.ok("删除成功", null) : R.fail("删除失败");
    }
}
