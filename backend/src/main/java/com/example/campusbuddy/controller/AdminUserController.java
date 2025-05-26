package com.example.campusbuddy.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.campusbuddy.common.R;
import com.example.campusbuddy.service.UserService;
import com.example.campusbuddy.service.NotificationService;
import com.example.campusbuddy.dto.NotificationCreateDTO;
import com.example.campusbuddy.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Tag(name = "管理员用户管理", description = "管理员用户管理相关操作")
@RestController
@RequestMapping("/api/admin/user")
public class AdminUserController {
    @Autowired
    private UserService userService;

    @Autowired
    private NotificationService notificationService;

    @Operation(summary = "分页查询用户（支持关键词和状态）")
    @GetMapping("/page")
    public R<Page<UserVO>> pageUsers(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status) {
        Page<UserVO> result = userService.adminPageUsers(page, size, keyword, status);
        return R.ok("查询成功", result);
    }

    @Operation(summary = "禁用/启用用户")
    @PostMapping("/{userId}/status")
    public R<Void> updateUserStatus(@PathVariable Long userId, @RequestParam String status) {
        boolean ok = userService.adminUpdateUserStatus(userId, status);
        if (ok) {
            // 发送通知
            NotificationCreateDTO dto = new NotificationCreateDTO();
            dto.setRecipientId(userId);
            dto.setType("USER_STATUS");
            dto.setTitle("账号状态变更");
            String statusText = "ACTIVE".equals(status) ? "启用" : "禁用";
            dto.setContent("您的账号已被管理员" + statusText + "。");
            notificationService.createUserNotification(dto);
        }
        return ok ? R.ok("操作成功", null) : R.fail("操作失败");
    }

    @Operation(summary = "重置用户密码")
    @PostMapping("/{userId}/reset-password")
    public R<Map<String, String>> resetPassword(@PathVariable Long userId) {
        String newPwd = userService.adminResetPassword(userId);
        Map<String, String> map = new HashMap<>();
        map.put("newPassword", newPwd);
        // 发送通知
        NotificationCreateDTO dto = new NotificationCreateDTO();
        dto.setRecipientId(userId);
        dto.setType("USER_PASSWORD_RESET");
        dto.setTitle("密码已被重置");
        dto.setContent("您的账号密码已被管理员重置，请尽快修改密码。");
        notificationService.createUserNotification(dto);
        return R.ok("密码已重置", map);
    }
}
