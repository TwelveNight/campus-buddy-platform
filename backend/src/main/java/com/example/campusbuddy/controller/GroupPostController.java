package com.example.campusbuddy.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.campusbuddy.common.R;
import com.example.campusbuddy.entity.GroupPost;
import com.example.campusbuddy.entity.User;
import com.example.campusbuddy.service.GroupPostService;
import com.example.campusbuddy.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Map;

@Tag(name = "学习小组帖子接口", description = "学习小组讨论区帖子相关操作")
@RestController
@RequestMapping("/api/group-posts")
public class GroupPostController {

    @Autowired
    private GroupPostService groupPostService;

    @Autowired
    private UserService userService;

    private static final Logger log = LoggerFactory.getLogger(GroupPostController.class);

    /**
     * 获取当前认证用户
     */
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userService.getUserByUsername(username);
    }

    /**
     * 获取小组帖子列表
     */
    @Operation(summary = "获取小组帖子列表", description = "分页获取指定小组的帖子")
    @GetMapping
    public R<IPage<GroupPost>> getGroupPosts(
            @Parameter(description = "小组ID") @RequestParam Long groupId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer pageSize) {

        IPage<GroupPost> posts = groupPostService.queryGroupPosts(groupId, pageNum, pageSize);
        return R.ok(posts);
    }

    /**
     * 获取帖子详情
     */
    @Operation(summary = "获取帖子详情")
    @GetMapping("/{postId}")
    public R<GroupPost> getPostDetail(@Parameter(description = "帖子ID") @PathVariable Long postId) {
        GroupPost post = groupPostService.getPostDetail(postId);
        if (post == null) {
            return R.fail("帖子不存在");
        }
        return R.ok(post);
    }

    /**
     * 发表帖子
     */
    @Operation(summary = "发表帖子", description = "在小组内发表新帖子")
    @PostMapping
    public R<Long> createPost(@org.springframework.web.bind.annotation.RequestBody Map<String, Object> postMap) {
        User currentUser = getCurrentUser();

        log.info("收到发帖请求原始数据: {}", postMap);

        // 手动构建GroupPost对象
        GroupPost post = new GroupPost();
        post.setAuthorId(currentUser.getUserId());

        // 从Map中获取groupId
        Object groupIdObj = postMap.get("groupId");
        Long groupId = null;
        if (groupIdObj != null) {
            try {
                if (groupIdObj instanceof Integer) {
                    groupId = Long.valueOf((Integer) groupIdObj);
                } else if (groupIdObj instanceof Long) {
                    groupId = (Long) groupIdObj;
                } else if (groupIdObj instanceof String) {
                    groupId = Long.parseLong((String) groupIdObj);
                }
            } catch (NumberFormatException e) {
                log.warn("无法解析groupId: {}", groupIdObj);
            }
        }
        post.setGroupId(groupId);

        // 获取其他字段
        if (postMap.containsKey("title")) {
            post.setTitle((String) postMap.get("title"));
        }
        if (postMap.containsKey("content")) {
            post.setContent((String) postMap.get("content"));
        }
        if (postMap.containsKey("contentType")) {
            post.setContentType((String) postMap.get("contentType"));
        }

        log.info("转换后的GroupPost: {}", post);

        // groupId 必须传递且大于0
        if (post.getGroupId() == null || post.getGroupId() <= 0) {
            log.warn("非法groupId: {}，原始post: {}", post.getGroupId(), post);
            return R.fail("缺少或非法的 groupId，无法发表帖子");
        }

        Long postId = groupPostService.createPost(post);
        return R.ok("帖子发表成功", postId);
    }

    /**
     * 更新帖子
     */
    @Operation(summary = "更新帖子", description = "仅作者可更新帖子内容")
    @PutMapping("/{postId}")
    public R<Void> updatePost(@Parameter(description = "帖子ID") @PathVariable Long postId,
            @org.springframework.web.bind.annotation.RequestBody GroupPost post) {
        User currentUser = getCurrentUser();
        post.setPostId(postId);
        post.setAuthorId(currentUser.getUserId());

        boolean success = groupPostService.updatePost(post);
        return success ? R.ok("帖子更新成功", null) : R.fail("更新失败，请确认您是帖子作者");
    }

    /**
     * 删除帖子
     */
    @Operation(summary = "删除帖子", description = "仅作者可删除帖子")
    @DeleteMapping("/{postId}")
    public R<Void> deletePost(@Parameter(description = "帖子ID") @PathVariable Long postId) {
        User currentUser = getCurrentUser();
        boolean success = groupPostService.deletePost(postId, currentUser.getUserId());

        return success ? R.ok("帖子删除成功", null) : R.fail("删除失败，请确认您有权限删除该帖子");
    }

    /**
     * 点赞帖子
     */
    @Operation(summary = "点赞帖子")
    @PostMapping("/{postId}/like")
    public R<Void> likePost(@Parameter(description = "帖子ID") @PathVariable Long postId) {
        User currentUser = getCurrentUser();
        boolean success = groupPostService.likePost(postId, currentUser.getUserId());

        return success ? R.ok("点赞成功", null) : R.fail("点赞失败");
    }

    /**
     * 取消点赞
     */
    @Operation(summary = "取消点赞")
    @PostMapping("/{postId}/unlike")
    public R<Void> unlikePost(@Parameter(description = "帖子ID") @PathVariable Long postId) {
        User currentUser = getCurrentUser();
        boolean success = groupPostService.unlikePost(postId, currentUser.getUserId());

        return success ? R.ok("已取消点赞", null) : R.fail("取消点赞失败");
    }
}
