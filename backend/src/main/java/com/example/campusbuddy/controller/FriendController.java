package com.example.campusbuddy.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.campusbuddy.common.R;
import com.example.campusbuddy.common.ResultCode;
import com.example.campusbuddy.dto.FriendRequestDTO;
import com.example.campusbuddy.service.FriendService;
import com.example.campusbuddy.vo.FriendRequestVO;
import com.example.campusbuddy.vo.FriendVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "好友接口", description = "好友相关操作")
@RestController
@RequestMapping("/api/friend")
public class FriendController {
    
    @Autowired
    private FriendService friendService;
    
    @Operation(summary = "发送好友申请")
    @PostMapping("/apply")
    public R<Map<String, Object>> applyFriend(HttpServletRequest request, @RequestBody FriendRequestDTO dto) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return R.fail(ResultCode.UNAUTHORIZED, "未登录");
        }
        
        try {
            Long requestId = friendService.applyFriend(userId, dto);
            Map<String, Object> data = Map.of(
                "requestId", requestId,
                "status", "SUCCESS"
            );
            return R.ok("好友申请发送成功", data);
        } catch (Exception e) {
            String msg = e.getMessage();
            String status = "ERROR";
            
            // 根据异常消息判断具体状态
            if (msg != null) {
                if (msg.contains("已经是好友") || msg.contains("已是好友")) {
                    status = "ALREADY_FRIEND";
                } else if (msg.contains("已发送过好友申请") || msg.contains("已申请")) {
                    status = "ALREADY_APPLIED";
                }
            }
            
            Map<String, Object> data = Map.of("status", status);
            return R.fail(ResultCode.BAD_REQUEST, msg, data);
        }
    }
    
    @Operation(summary = "接受好友申请")
    @PostMapping("/accept/{requestId}")
    public R<Boolean> acceptFriendRequest(HttpServletRequest request, @PathVariable Long requestId) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return R.fail(ResultCode.UNAUTHORIZED, "未登录");
        }
        
        try {
            boolean result = friendService.acceptFriendRequest(userId, requestId);
            return R.ok("好友申请已接受", result);
        } catch (Exception e) {
            return R.fail(e.getMessage());
        }
    }
    
    @Operation(summary = "拒绝好友申请")
    @PostMapping("/reject/{requestId}")
    public R<Boolean> rejectFriendRequest(HttpServletRequest request, @PathVariable Long requestId) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return R.fail(ResultCode.UNAUTHORIZED, "未登录");
        }
        
        try {
            boolean result = friendService.rejectFriendRequest(userId, requestId);
            return R.ok("好友申请已拒绝", result);
        } catch (Exception e) {
            return R.fail(e.getMessage());
        }
    }
    
    @Operation(summary = "获取好友列表")
    @GetMapping("/list")
    public R<List<FriendVO>> getFriendList(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return R.fail(ResultCode.UNAUTHORIZED, "未登录");
        }
        
        List<FriendVO> friends = friendService.getFriendList(userId);
        return R.ok("获取好友列表成功", friends);
    }
    
    @Operation(summary = "获取好友ID列表")
    @GetMapping("/ids")
    public R<List<Long>> getFriendIds(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return R.fail(ResultCode.UNAUTHORIZED, "未登录");
        }
        
        List<Long> friendIds = friendService.getFriendIds(userId);
        return R.ok("获取好友ID列表成功", friendIds);
    }
    
    @Operation(summary = "获取好友申请列表")
    @GetMapping("/requests")
    public R<List<FriendRequestVO>> getFriendRequests(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return R.fail(ResultCode.UNAUTHORIZED, "未登录");
        }
        
        List<FriendRequestVO> requests = friendService.getFriendRequests(userId);
        return R.ok("获取好友申请列表成功", requests);
    }
    
    @Operation(summary = "删除好友")
    @DeleteMapping("/{friendId}")
    public R<Boolean> deleteFriend(HttpServletRequest request, @PathVariable Long friendId) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return R.fail(ResultCode.UNAUTHORIZED, "未登录");
        }
        
        try {
            boolean result = friendService.deleteFriend(userId, friendId);
            return R.ok("好友删除成功", result);
        } catch (Exception e) {
            return R.fail(e.getMessage());
        }
    }
    
    @Operation(summary = "搜索好友")
    @GetMapping("/search")
    public R<Page<FriendVO>> searchFriends(
            HttpServletRequest request,
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return R.fail(ResultCode.UNAUTHORIZED, "未登录");
        }
        
        Page<FriendVO> friends = friendService.searchFriends(userId, keyword, page, size);
        return R.ok("搜索好友成功", friends);
    }
    
    @Operation(summary = "检查是否是好友")
    @GetMapping("/check/{targetId}")
    public R<Map<String, Object>> checkFriendStatus(HttpServletRequest request, @PathVariable Long targetId) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return R.fail(ResultCode.UNAUTHORIZED, "未登录");
        }
        
        boolean isFriend = friendService.isFriend(userId, targetId);
        String requestStatus = friendService.getFriendRequestStatus(userId, targetId);
        
        // 标准化 requestStatus
        String normalizedStatus = "NONE";
        if (isFriend) {
            normalizedStatus = "ACCEPTED";
        } else if (requestStatus != null) {
            if (requestStatus.contains("PENDING") || requestStatus.contains("待处理")) {
                // 判断是自己发出的还是收到的
                if (requestStatus.contains("OUTGOING") || requestStatus.contains("发出")) {
                    normalizedStatus = "PENDING_OUTGOING";
                } else if (requestStatus.contains("INCOMING") || requestStatus.contains("收到")) {
                    normalizedStatus = "PENDING_INCOMING";
                } else {
                    normalizedStatus = "PENDING_OUTGOING"; // 默认为发出
                }
            } else if (requestStatus.contains("REJECTED") || requestStatus.contains("拒绝")) {
                normalizedStatus = "REJECTED";
            }
        }
        
        return R.ok("获取好友状态成功", Map.of(
                "isFriend", isFriend,
                "requestStatus", normalizedStatus
        ));
    }
}
