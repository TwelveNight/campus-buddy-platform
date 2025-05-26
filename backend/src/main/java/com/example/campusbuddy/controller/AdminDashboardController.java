package com.example.campusbuddy.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.campusbuddy.common.R;
import com.example.campusbuddy.common.ResultCode;
import com.example.campusbuddy.entity.Group;
import com.example.campusbuddy.entity.GroupPost;
import com.example.campusbuddy.entity.PostComment;
import com.example.campusbuddy.entity.User;
import com.example.campusbuddy.service.GroupPostService;
import com.example.campusbuddy.service.GroupService;
import com.example.campusbuddy.service.PostCommentService;
import com.example.campusbuddy.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Tag(name = "管理员数据统计", description = "管理员数据统计相关操作")
@RestController
@RequestMapping("/api/admin/dashboard")
public class AdminDashboardController {
    @Autowired
    private UserService userService;
    
    @Autowired
    private GroupService groupService;
    
    @Autowired
    private GroupPostService groupPostService;
    
    @Autowired
    private PostCommentService postCommentService;
    
    @Operation(summary = "获取平台概览统计数据")
    @GetMapping("/overview")
    public R<Map<String, Object>> getOverviewStats(HttpServletRequest request) {
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
        
        Map<String, Object> stats = new HashMap<>();
        
        // 用户总数
        long totalUsers = userService.count();
        stats.put("totalUsers", totalUsers);
        
        // 帖子总数
        long totalPosts = groupPostService.count();
        stats.put("totalPosts", totalPosts);
        
        // 评论总数
        long totalComments = postCommentService.count();
        stats.put("totalComments", totalComments);
        
        // 群组总数
        long totalGroups = groupService.count();
        stats.put("totalGroups", totalGroups);
        
        return R.ok("获取统计数据成功", stats);
    }
    
    @Operation(summary = "获取用户统计数据")
    @GetMapping("/users")
    public R<Map<String, Object>> getUserStats(HttpServletRequest request) {
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
        
        Map<String, Object> stats = new HashMap<>();
        
        // 用户总数
        long totalUsers = userService.count();
        stats.put("totalUsers", totalUsers);
        
        // 用户状态统计
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("status as name, count(*) as value");
        queryWrapper.groupBy("status");
        List<Map<String, Object>> statusCounts = userService.listMaps(queryWrapper);
        stats.put("statusCounts", statusCounts);
        
        // 最近7天注册用户统计
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime sevenDaysAgo = now.minusDays(7);
        
        QueryWrapper<User> registerQueryWrapper = new QueryWrapper<>();
        registerQueryWrapper.select("date_format(created_at, '%Y-%m-%d') as date, count(*) as count");
        registerQueryWrapper.ge("created_at", sevenDaysAgo);
        registerQueryWrapper.groupBy("date_format(created_at, '%Y-%m-%d')");
        registerQueryWrapper.orderByAsc("date");
        
        List<Map<String, Object>> registrationTrend = userService.listMaps(registerQueryWrapper);
        
        // 填充没有数据的日期
        Map<String, Integer> registrationMap = registrationTrend.stream()
                .collect(Collectors.toMap(
                        m -> (String) m.get("date"),
                        m -> ((Long) m.get("count")).intValue()
                ));
        
        List<Map<String, Object>> completeRegistrationTrend = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        for (int i = 6; i >= 0; i--) {
            String date = now.minusDays(i).format(formatter);
            int count = registrationMap.getOrDefault(date, 0);
            
            Map<String, Object> dayData = new HashMap<>();
            dayData.put("date", date);
            dayData.put("count", count);
            completeRegistrationTrend.add(dayData);
        }
        
        stats.put("registrationTrend", completeRegistrationTrend);
        
        return R.ok("获取用户统计数据成功", stats);
    }
    
    @Operation(summary = "获取帖子统计数据")
    @GetMapping("/posts")
    public R<Map<String, Object>> getPostStats(HttpServletRequest request) {
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
        
        Map<String, Object> stats = new HashMap<>();
        
        // 帖子总数
        long totalPosts = groupPostService.count();
        stats.put("totalPosts", totalPosts);
        
        // 评论总数
        long totalComments = postCommentService.count();
        stats.put("totalComments", totalComments);
        
        // 帖子状态统计
        QueryWrapper<GroupPost> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("status as name, count(*) as value");
        queryWrapper.groupBy("status");
        List<Map<String, Object>> statusCounts = groupPostService.listMaps(queryWrapper);
        stats.put("statusCounts", statusCounts);
        
        // 最近7天发帖趋势
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime sevenDaysAgo = now.minusDays(7);
        
        QueryWrapper<GroupPost> postQueryWrapper = new QueryWrapper<>();
        postQueryWrapper.select("date_format(created_at, '%Y-%m-%d') as date, count(*) as count");
        postQueryWrapper.ge("created_at", sevenDaysAgo);
        postQueryWrapper.groupBy("date_format(created_at, '%Y-%m-%d')");
        postQueryWrapper.orderByAsc("date");
        
        List<Map<String, Object>> postTrend = groupPostService.listMaps(postQueryWrapper);
        
        // 填充没有数据的日期
        Map<String, Integer> postMap = postTrend.stream()
                .collect(Collectors.toMap(
                        m -> (String) m.get("date"),
                        m -> ((Long) m.get("count")).intValue()
                ));
        
        List<Map<String, Object>> completePostTrend = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        for (int i = 6; i >= 0; i--) {
            String date = now.minusDays(i).format(formatter);
            int count = postMap.getOrDefault(date, 0);
            
            Map<String, Object> dayData = new HashMap<>();
            dayData.put("date", date);
            dayData.put("count", count);
            completePostTrend.add(dayData);
        }
        
        stats.put("postTrend", completePostTrend);
        
        return R.ok("获取帖子统计数据成功", stats);
    }
    
    @Operation(summary = "获取群组统计数据")
    @GetMapping("/groups")
    public R<Map<String, Object>> getGroupStats(HttpServletRequest request) {
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
        
        Map<String, Object> stats = new HashMap<>();
        
        // 群组总数
        long totalGroups = groupService.count();
        stats.put("totalGroups", totalGroups);
        
        // 群组状态统计
        QueryWrapper<Group> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("status as name, count(*) as value");
        queryWrapper.groupBy("status");
        List<Map<String, Object>> statusCounts = groupService.listMaps(queryWrapper);
        stats.put("statusCounts", statusCounts);
        
        // 最近7天创建群组趋势
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime sevenDaysAgo = now.minusDays(7);
        
        QueryWrapper<Group> groupQueryWrapper = new QueryWrapper<>();
        groupQueryWrapper.select("date_format(created_at, '%Y-%m-%d') as date, count(*) as count");
        groupQueryWrapper.ge("created_at", sevenDaysAgo);
        groupQueryWrapper.groupBy("date_format(created_at, '%Y-%m-%d')");
        groupQueryWrapper.orderByAsc("date");
        
        List<Map<String, Object>> groupTrend = groupService.listMaps(groupQueryWrapper);
        
        // 填充没有数据的日期
        Map<String, Integer> groupMap = groupTrend.stream()
                .collect(Collectors.toMap(
                        m -> (String) m.get("date"),
                        m -> ((Long) m.get("count")).intValue()
                ));
        
        List<Map<String, Object>> completeGroupTrend = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        for (int i = 6; i >= 0; i--) {
            String date = now.minusDays(i).format(formatter);
            int count = groupMap.getOrDefault(date, 0);
            
            Map<String, Object> dayData = new HashMap<>();
            dayData.put("date", date);
            dayData.put("count", count);
            completeGroupTrend.add(dayData);
        }
        
        stats.put("groupTrend", completeGroupTrend);
        
        return R.ok("获取群组统计数据成功", stats);
    }
}
