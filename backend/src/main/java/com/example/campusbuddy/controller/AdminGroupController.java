package com.example.campusbuddy.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.campusbuddy.common.R;
import com.example.campusbuddy.entity.Group;
import com.example.campusbuddy.service.GroupService;
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

    @Operation(summary = "分页查询小组（支持关键词和状态）")
    @GetMapping("/page")
    public R<Page<Group>> pageGroups(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            HttpServletRequest request) {
        // 验证管理员权限
        List<String> roles = (List<String>) request.getAttribute("roles");
        boolean isAdmin = roles != null && roles.contains("ROLE_ADMIN");
        
        if (!isAdmin) {
            return R.fail("权限不足，需要管理员权限");
        }
        
        Page<Group> result = groupService.adminPageGroups(page, size, keyword, status);
        return R.ok("查询成功", result);
    }

    @Operation(summary = "禁用/启用小组")
    @PostMapping("/{groupId}/status")
    public R<Void> updateGroupStatus(
            @PathVariable Long groupId, 
            @RequestParam String status,
            HttpServletRequest request) {
        // 验证管理员权限
        List<String> roles = (List<String>) request.getAttribute("roles");
        boolean isAdmin = roles != null && roles.contains("ROLE_ADMIN");
        
        if (!isAdmin) {
            return R.fail("权限不足，需要管理员权限");
        }
        
        boolean ok = groupService.adminUpdateGroupStatus(groupId, status);
        return ok ? R.ok("操作成功", null) : R.fail("操作失败");
    }

    @Operation(summary = "删除小组")
    @DeleteMapping("/{groupId}")
    public R<Void> deleteGroup(
            @PathVariable Long groupId,
            HttpServletRequest request) {
        // 验证管理员权限
        List<String> roles = (List<String>) request.getAttribute("roles");
        boolean isAdmin = roles != null && roles.contains("ROLE_ADMIN");
        
        if (!isAdmin) {
            return R.fail("权限不足，需要管理员权限");
        }
        
        boolean ok = groupService.adminDeleteGroup(groupId);
        return ok ? R.ok("删除成功", null) : R.fail("删除失败");
    }
}
