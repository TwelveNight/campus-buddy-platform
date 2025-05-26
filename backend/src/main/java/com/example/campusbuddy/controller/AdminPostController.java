package com.example.campusbuddy.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.campusbuddy.common.R;
import com.example.campusbuddy.dto.NotificationCreateDTO;
import com.example.campusbuddy.entity.Group;
import com.example.campusbuddy.entity.GroupPost;
import com.example.campusbuddy.entity.User;
import com.example.campusbuddy.service.GroupPostService;
import com.example.campusbuddy.service.GroupService;
import com.example.campusbuddy.service.NotificationService;
import com.example.campusbuddy.service.UserService;
import com.example.campusbuddy.vo.GroupPostVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Tag(name = "管理员帖子管理", description = "管理员帖子管理相关操作")
@RestController
@RequestMapping("/api/admin/post")
public class AdminPostController {
    
    @Autowired
    private GroupPostService groupPostService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private GroupService groupService;

    @Autowired
    private NotificationService notificationService;

    @Operation(summary = "分页查询帖子（支持关键词、小组ID和状态）")
    @GetMapping("/page")
    public R<Page<GroupPostVO>> pagePosts(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long groupId,
            @RequestParam(required = false) String status,
            HttpServletRequest request) {
        // 验证管理员权限
        @SuppressWarnings("unchecked")
        List<String> roles = (List<String>) request.getAttribute("roles");
        boolean isAdmin = roles != null && roles.contains("ROLE_ADMIN");
        
        if (!isAdmin) {
            return R.fail("权限不足，需要管理员权限");
        }
        
        // 使用 adminPagePosts 获取基本帖子数据
        Page<GroupPost> postPage = groupPostService.adminPagePosts(page, size, keyword, groupId, status);
        
        // 转换为 GroupPostVO 并添加作者和小组信息
        List<Long> authorIds = postPage.getRecords().stream().map(GroupPost::getAuthorId).collect(Collectors.toList());
        List<Long> groupIds = postPage.getRecords().stream().map(GroupPost::getGroupId).collect(Collectors.toList());
        
        Map<Long, User> userMap = userService.listByIds(authorIds).stream().collect(Collectors.toMap(User::getUserId, user -> user));
        Map<Long, Group> groupMap = groupService.listByIds(groupIds).stream().collect(Collectors.toMap(Group::getGroupId, group -> group));
        
        // 创建转换后的页面
        Page<GroupPostVO> voPage = new Page<>(postPage.getCurrent(), postPage.getSize(), postPage.getTotal());
        List<GroupPostVO> voList = postPage.getRecords().stream().map(post -> {
            GroupPostVO vo = new GroupPostVO();
            vo.setPostId(post.getPostId());
            vo.setGroupId(post.getGroupId());
            vo.setAuthorId(post.getAuthorId());
            vo.setTitle(post.getTitle());
            vo.setContent(post.getContent());
            vo.setContentType(post.getContentType());
            vo.setLikeCount(post.getLikeCount());
            vo.setCommentCount(post.getCommentCount());
            vo.setStatus(post.getStatus());
            vo.setCreatedAt(post.getCreatedAt());
            vo.setUpdatedAt(post.getUpdatedAt());
            
            // 添加作者信息
            User author = userMap.get(post.getAuthorId());
            if (author != null) {
                vo.setAuthorName(author.getNickname() != null ? author.getNickname() : author.getUsername());
                vo.setAuthorAvatar(author.getAvatarUrl());
            } else {
                vo.setAuthorName("未知用户");
            }
            
            // 添加小组信息
            Group group = groupMap.get(post.getGroupId());
            if (group != null) {
                vo.setGroupName(group.getName());
                vo.setGroupAvatar(group.getAvatarUrl());
            } else {
                vo.setGroupName("未知小组");
            }
            
            return vo;
        }).collect(Collectors.toList());
        
        voPage.setRecords(voList);
        return R.ok("查询成功", voPage);
    }

    @Operation(summary = "更新帖子状态")
    @PostMapping("/{postId}/status")
    public R<Void> updatePostStatus(
            @PathVariable Long postId, 
            @RequestParam String status,
            HttpServletRequest request) {
        // 验证管理员权限
        @SuppressWarnings("unchecked")
        List<String> roles = (List<String>) request.getAttribute("roles");
        boolean isAdmin = roles != null && roles.contains("ROLE_ADMIN");
        
        if (!isAdmin) {
            return R.fail("权限不足，需要管理员权限");
        }
        
        boolean ok = groupPostService.adminUpdatePostStatus(postId, status);
        if (ok) {
            // 获取帖子，通知作者
            GroupPost post = groupPostService.getById(postId);
            if (post != null && post.getAuthorId() != null) {
                NotificationCreateDTO dto = new NotificationCreateDTO();
                dto.setRecipientId(post.getAuthorId());
                dto.setType("POST_STATUS");
                dto.setTitle("帖子状态变更");
                String statusText = "ACTIVE".equals(status) ? "启用" : "禁用";
                dto.setContent("您的帖子(ID:" + postId + ")已被管理员" + statusText + "。");
                dto.setRelatedId(postId);
                notificationService.createUserNotification(dto);
            }
        }
        return ok ? R.ok("操作成功", null) : R.fail("操作失败");
    }

    @Operation(summary = "删除帖子")
    @DeleteMapping("/{postId}")
    public R<Void> deletePost(
            @PathVariable Long postId,
            HttpServletRequest request) {
        // 验证管理员权限
        @SuppressWarnings("unchecked")
        List<String> roles = (List<String>) request.getAttribute("roles");
        boolean isAdmin = roles != null && roles.contains("ROLE_ADMIN");
        
        if (!isAdmin) {
            return R.fail("权限不足，需要管理员权限");
        }
        
        // 获取帖子，通知作者
        GroupPost post = groupPostService.getById(postId);
        Long authorId = post != null ? post.getAuthorId() : null;
        boolean ok = groupPostService.adminDeletePost(postId);
        if (ok && authorId != null) {
            NotificationCreateDTO dto = new NotificationCreateDTO();
            dto.setRecipientId(authorId);
            dto.setType("POST_DELETE");
            dto.setTitle("帖子被删除");
            dto.setContent("您的帖子(ID:" + postId + ")已被管理员删除。");
            dto.setRelatedId(postId);
            notificationService.createUserNotification(dto);
        }
        return ok ? R.ok("删除成功", null) : R.fail("删除失败");
    }
}
