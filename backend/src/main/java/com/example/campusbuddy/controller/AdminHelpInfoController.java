package com.example.campusbuddy.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.campusbuddy.common.R;
import com.example.campusbuddy.common.ResultCode;
import com.example.campusbuddy.service.HelpInfoService;
import com.example.campusbuddy.service.NotificationService;
import com.example.campusbuddy.vo.HelpInfoVO;
import com.example.campusbuddy.dto.NotificationCreateDTO;
import com.example.campusbuddy.entity.HelpInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "管理员互助任务管理", description = "管理员互助任务管理相关操作")
@RestController
@RequestMapping("/api/admin/helpinfo")
public class AdminHelpInfoController {

    @Autowired
    private HelpInfoService helpInfoService;

    @Autowired
    private NotificationService notificationService;

    @Operation(summary = "管理员分页查询互助任务")
    @GetMapping("/page")
    public R<Page<HelpInfoVO>> adminPageHelpInfo(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            HttpServletRequest request) {
        
        // 检查管理员权限
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return R.fail(ResultCode.UNAUTHORIZED, "未登录");
        }
        
        List<String> roles = (List<String>) request.getAttribute("roles");
        boolean isAdmin = roles != null && roles.contains("ROLE_ADMIN");
        
        if (!isAdmin) {
            return R.fail(ResultCode.FORBIDDEN, "无管理员权限");
        }

        Page<HelpInfoVO> result = helpInfoService.adminPageHelpInfo(page, size, keyword, type, status);
        return R.ok("查询成功", result);
    }

    @Operation(summary = "管理员更新互助任务状态")
    @PatchMapping("/{id}/status")
    public R<Void> adminUpdateStatus(
            @PathVariable Long id,
            @RequestParam String status,
            HttpServletRequest request) {
        
        // 检查管理员权限
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return R.fail(ResultCode.UNAUTHORIZED, "未登录");
        }
        
        List<String> roles = (List<String>) request.getAttribute("roles");
        boolean isAdmin = roles != null && roles.contains("ROLE_ADMIN");
        
        if (!isAdmin) {
            return R.fail(ResultCode.FORBIDDEN, "无管理员权限");
        }

        boolean success = helpInfoService.adminUpdateStatus(id, status);
        if (success) {
            // 获取互助任务，通知发布者
            HelpInfo helpInfo = helpInfoService.getById(id);
            if (helpInfo != null && helpInfo.getPublisherId() != null) {
                NotificationCreateDTO dto = new NotificationCreateDTO();
                dto.setRecipientId(helpInfo.getPublisherId());
                dto.setType("HELPINFO_STATUS");
                dto.setTitle("互助任务状态变更");
                String statusText = status;
                dto.setContent("您的互助任务(ID:" + id + ")状态已被管理员更改为：" + statusText + "。");
                dto.setRelatedId(id);
                notificationService.createUserNotification(dto);
            }
        }
        return success ? R.ok("状态更新成功", null) : R.fail("状态更新失败");
    }

    @Operation(summary = "管理员删除互助任务")
    @DeleteMapping("/{id}")
    public R<Void> adminDeleteHelpInfo(
            @PathVariable Long id,
            HttpServletRequest request) {
        
        // 检查管理员权限
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return R.fail(ResultCode.UNAUTHORIZED, "未登录");
        }
        
        List<String> roles = (List<String>) request.getAttribute("roles");
        boolean isAdmin = roles != null && roles.contains("ROLE_ADMIN");
        
        if (!isAdmin) {
            return R.fail(ResultCode.FORBIDDEN, "无管理员权限");
        }

        // 获取互助任务，通知发布者
        HelpInfo helpInfo = helpInfoService.getById(id);
        Long publisherId = helpInfo != null ? helpInfo.getPublisherId() : null;
        boolean success = helpInfoService.removeById(id);
        if (success && publisherId != null) {
            NotificationCreateDTO dto = new NotificationCreateDTO();
            dto.setRecipientId(publisherId);
            dto.setType("HELPINFO_DELETE");
            dto.setTitle("互助任务被删除");
            dto.setContent("您的互助任务(ID:" + id + ")已被管理员删除。");
            dto.setRelatedId(id);
            notificationService.createUserNotification(dto);
        }
        return success ? R.ok("删除成功", null) : R.fail("删除失败");
    }
}
