package com.example.campusbuddy.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.campusbuddy.common.R;
import com.example.campusbuddy.entity.GroupPost;
import com.example.campusbuddy.service.GroupPostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "管理员帖子管理", description = "管理员帖子管理相关操作")
@RestController
@RequestMapping("/api/admin/post")
public class AdminPostController {
    
    @Autowired
    private GroupPostService groupPostService;

    @Operation(summary = "分页查询帖子（支持关键词、小组ID和状态）")
    @GetMapping("/page")
    public R<Page<GroupPost>> pagePosts(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long groupId,
            @RequestParam(required = false) String status,
            HttpServletRequest request) {
        // 验证管理员权限
        List<String> roles = (List<String>) request.getAttribute("roles");
        boolean isAdmin = roles != null && roles.contains("ROLE_ADMIN");
        
        if (!isAdmin) {
            return R.fail("权限不足，需要管理员权限");
        }
        
        Page<GroupPost> result = groupPostService.adminPagePosts(page, size, keyword, groupId, status);
        return R.ok("查询成功", result);
    }

    @Operation(summary = "更新帖子状态")
    @PostMapping("/{postId}/status")
    public R<Void> updatePostStatus(
            @PathVariable Long postId, 
            @RequestParam String status,
            HttpServletRequest request) {
        // 验证管理员权限
        List<String> roles = (List<String>) request.getAttribute("roles");
        boolean isAdmin = roles != null && roles.contains("ROLE_ADMIN");
        
        if (!isAdmin) {
            return R.fail("权限不足，需要管理员权限");
        }
        
        boolean ok = groupPostService.adminUpdatePostStatus(postId, status);
        return ok ? R.ok("操作成功", null) : R.fail("操作失败");
    }

    @Operation(summary = "删除帖子")
    @DeleteMapping("/{postId}")
    public R<Void> deletePost(
            @PathVariable Long postId,
            HttpServletRequest request) {
        // 验证管理员权限
        List<String> roles = (List<String>) request.getAttribute("roles");
        boolean isAdmin = roles != null && roles.contains("ROLE_ADMIN");
        
        if (!isAdmin) {
            return R.fail("权限不足，需要管理员权限");
        }
        
        boolean ok = groupPostService.adminDeletePost(postId);
        return ok ? R.ok("删除成功", null) : R.fail("删除失败");
    }
}
