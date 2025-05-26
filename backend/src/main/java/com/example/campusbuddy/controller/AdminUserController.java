package com.example.campusbuddy.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.campusbuddy.common.R;
import com.example.campusbuddy.service.UserService;
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
        return ok ? R.ok("操作成功", null) : R.fail("操作失败");
    }

    @Operation(summary = "重置用户密码")
    @PostMapping("/{userId}/reset-password")
    public R<Map<String, String>> resetPassword(@PathVariable Long userId) {
        String newPwd = userService.adminResetPassword(userId);
        Map<String, String> map = new HashMap<>();
        map.put("newPassword", newPwd);
        return R.ok("密码已重置", map);
    }
}
