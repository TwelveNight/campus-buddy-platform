package com.example.campusbuddy.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.campusbuddy.common.R;
import com.example.campusbuddy.dto.NotificationCreateDTO;
import com.example.campusbuddy.entity.Notification;
import com.example.campusbuddy.entity.User;
import com.example.campusbuddy.service.NotificationService;
import com.example.campusbuddy.service.UserService;
import com.example.campusbuddy.vo.NotificationVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 消息通知控制器
 */
@Tag(name = "通知接口", description = "消息通知相关操作")
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private UserService userService;

    /**
     * 获取当前用户的通知列表
     */
    @Operation(summary = "获取通知列表", description = "分页获取当前用户的通知列表")
    @GetMapping
    public R<IPage<NotificationVO>> getNotifications(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "通知类型") @RequestParam(required = false) String type,
            @Parameter(description = "是否已读") @RequestParam(required = false) Boolean isRead, // 添加 isRead 参数
            HttpServletRequest request) {
        
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return R.fail("用户未登录");
        }
        
        IPage<NotificationVO> notifications = notificationService.getUserNotifications(userId, page, size, type, isRead); // 传递 isRead 参数
        return R.ok(notifications);
    }

    /**
     * 获取未读通知数量
     */
    @Operation(summary = "获取未读通知数量")
    @GetMapping("/unread/count")
    public R<Map<String, Integer>> getUnreadCount(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return R.fail("用户未登录");
        }
        
        int count = notificationService.countUnreadNotifications(userId);
        Map<String, Integer> result = new HashMap<>();
        result.put("count", count);
        
        return R.ok(result);
    }

    /**
     * 标记通知为已读
     */
    @Operation(summary = "标记通知为已读")
    @PutMapping("/{notificationId}/read")
    public R<Void> markAsRead(
            @Parameter(description = "通知ID") @PathVariable Long notificationId,
            HttpServletRequest request) {
        
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return R.fail("用户未登录");
        }
        
        boolean success = notificationService.markAsRead(notificationId, userId);
        if (success) {
            return R.ok("已标记为已读",null);
        } else {
            return R.fail("标记已读失败");
        }
    }

    /**
     * 标记所有通知为已读
     */
    @Operation(summary = "标记所有通知为已读")
    @PutMapping("/read/all")
    public R<Map<String, Integer>> markAllAsRead(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return R.fail("用户未登录");
        }
        
        int count = notificationService.markAllAsRead(userId);
        Map<String, Integer> result = new HashMap<>();
        result.put("count", count);
        
        return R.ok("已将所有通知标记为已读", result);
    }

    /**
     * 删除通知
     */
    @Operation(summary = "删除通知")
    @DeleteMapping("/{notificationId}")
    public R<Void> deleteNotification(
            @Parameter(description = "通知ID") @PathVariable Long notificationId,
            HttpServletRequest request) {
        
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return R.fail("用户未登录");
        }
        
        boolean success = notificationService.deleteNotification(notificationId, userId);
        if (success) {
            return R.ok("删除成功",null);
        } else {
            return R.fail("删除失败");
        }
    }

    /**
     * 发送系统通知（仅管理员可用）
     */
    @Operation(summary = "发送系统通知", description = "向指定用户或所有用户发送系统通知（仅管理员可用）")
    @PostMapping("/system")
    public R<Long> sendSystemNotification(
            @RequestBody NotificationCreateDTO dto,
            HttpServletRequest request) {
        
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return R.fail("用户未登录");
        }
        
        // 检查是否为管理员
        boolean isAdmin = userService.isAdmin(userId);
        if (!isAdmin) {
            return R.fail("权限不足，仅管理员可发送系统通知");
        }
        
        // 如果recipientId为0或null，视为系统公告，给所有活跃用户发送通知
        if (dto.getRecipientId() == null || dto.getRecipientId() == 0) {
            // 获取所有活跃用户
            List<User> activeUsers = userService.list(
                new LambdaQueryWrapper<User>()
                    .eq(User::getStatus, "ACTIVE")
            );
            
            Long firstNotificationId = null;
            
            // 为每个用户创建一条系统通知
            for (User user : activeUsers) {
                NotificationCreateDTO userDto = new NotificationCreateDTO();
                BeanUtils.copyProperties(dto, userDto);
                userDto.setRecipientId(user.getUserId());
                
                Long notificationId = notificationService.createSystemNotification(userDto);
                if (firstNotificationId == null) {
                    firstNotificationId = notificationId;
                }
            }
            
            return R.ok("系统公告发送成功", firstNotificationId);
        }
        
        // 给特定用户发送通知
        Long notificationId = notificationService.createSystemNotification(dto);
        return R.ok("发送成功", notificationId);
    }
}
