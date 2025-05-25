package com.example.campusbuddy.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.campusbuddy.common.R;
import com.example.campusbuddy.dto.PrivateMessageDTO;
import com.example.campusbuddy.service.PrivateMessageService;
import com.example.campusbuddy.service.UserService;
import com.example.campusbuddy.vo.PrivateMessageVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 私信控制器
 */
@Tag(name = "私信接口", description = "用户私信相关操作")
@RestController
@RequestMapping("/api/messages")
public class PrivateMessageController {

    @Autowired
    private PrivateMessageService privateMessageService;

    @Autowired
    private UserService userService;

    /**
     * 发送私信
     */
    @Operation(summary = "发送私信")
    @PostMapping
    public R<Long> sendMessage(
            @RequestBody PrivateMessageDTO dto,
            HttpServletRequest request) {
        
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return R.fail("用户未登录");
        }
        
        // 检查接收者是否存在
        if (!userService.exists(dto.getRecipientId())) {
            return R.fail("接收者不存在");
        }
        
        Long messageId = privateMessageService.sendMessage(userId, dto);
        return R.ok("发送成功", messageId);
    }

    /**
     * 获取与指定用户的聊天记录
     */
    @Operation(summary = "获取聊天记录")
    @GetMapping("/chat/{userId}")
    public R<IPage<PrivateMessageVO>> getChatHistory(
            @Parameter(description = "对方用户ID") @PathVariable Long userId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") Integer size,
            HttpServletRequest request) {
        
        Long currentUserId = (Long) request.getAttribute("userId");
        if (currentUserId == null) {
            return R.fail("用户未登录");
        }
        
        // 检查对方用户是否存在
        if (!userService.exists(userId)) {
            return R.fail("用户不存在");
        }
        
        IPage<PrivateMessageVO> messages = privateMessageService.getChatHistory(currentUserId, userId, page, size);
        return R.ok(messages);
    }

    /**
     * 获取聊天会话列表
     */
    @Operation(summary = "获取聊天会话列表")
    @GetMapping("/sessions")
    public R<IPage<Map<String, Object>>> getChatSessions(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer size,
            HttpServletRequest request) {
        
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return R.fail("用户未登录");
        }
        
        IPage<Map<String, Object>> sessions = privateMessageService.getChatSessionList(userId, page, size);
        return R.ok(sessions);
    }

    /**
     * 获取未读私信数量
     */
    @Operation(summary = "获取未读私信数量")
    @GetMapping("/unread/count")
    public R<Map<String, Integer>> getUnreadCount(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return R.fail("用户未登录");
        }
        
        int count = privateMessageService.countUnreadMessages(userId);
        Map<String, Integer> result = new HashMap<>();
        result.put("count", count);
        
        return R.ok(result);
    }

    /**
     * 标记与指定用户的所有私信为已读
     */
    @Operation(summary = "标记与指定用户的所有私信为已读")
    @PutMapping("/read/all/{userId}")
    public R<Map<String, Integer>> markAllAsRead(
            @Parameter(description = "对方用户ID") @PathVariable Long userId,
            HttpServletRequest request) {
        
        Long currentUserId = (Long) request.getAttribute("userId");
        if (currentUserId == null) {
            return R.fail("用户未登录");
        }
        
        int count = privateMessageService.markAllAsRead(currentUserId, userId);
        Map<String, Integer> result = new HashMap<>();
        result.put("count", count);
        
        return R.ok("已将全部消息标记为已读", result);
    }

    /**
     * 标记指定私信为已读
     */
    @Operation(summary = "标记私信为已读")
    @PutMapping("/{messageId}/read")
    public R<Void> markAsRead(
            @Parameter(description = "消息ID") @PathVariable Long messageId,
            HttpServletRequest request) {
        
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return R.fail("用户未登录");
        }
        
        boolean success = privateMessageService.markAsRead(messageId, userId);
        if (success) {
            return R.ok("已标记为已读",null);
        } else {
            return R.fail("标记已读失败");
        }
    }

    /**
     * 删除私信
     */
    @Operation(summary = "删除私信")
    @DeleteMapping("/{messageId}")
    public R<Void> deleteMessage(
            @Parameter(description = "消息ID") @PathVariable Long messageId,
            HttpServletRequest request) {
        
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return R.fail("用户未登录");
        }
        
        boolean success = privateMessageService.deleteMessage(messageId, userId);
        if (success) {
            return R.ok("删除成功",null);
        } else {
            return R.fail("删除失败");
        }
    }
}
