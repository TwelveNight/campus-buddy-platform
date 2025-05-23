package com.example.campusbuddy.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.campusbuddy.common.R;
import com.example.campusbuddy.entity.GroupPost;
import com.example.campusbuddy.entity.User;
import com.example.campusbuddy.service.GroupPostService;
import com.example.campusbuddy.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/group-posts")
public class GroupPostController {

    @Autowired
    private GroupPostService groupPostService;

    @Autowired
    private UserService userService;

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
    @GetMapping
    public R<IPage<GroupPost>> getGroupPosts(
            @RequestParam Long groupId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        
        IPage<GroupPost> posts = groupPostService.queryGroupPosts(groupId, pageNum, pageSize);
        return R.ok(posts);
    }

    /**
     * 获取帖子详情
     */
    @GetMapping("/{postId}")
    public R<GroupPost> getPostDetail(@PathVariable Long postId) {
        GroupPost post = groupPostService.getPostDetail(postId);
        if (post == null) {
            return R.fail("帖子不存在");
        }
        return R.ok(post);
    }

    /**
     * 发表帖子
     */
    @PostMapping
    public R<Long> createPost(@RequestBody GroupPost post) {
        User currentUser = getCurrentUser();
        post.setAuthorId(currentUser.getUserId());
        
        Long postId = groupPostService.createPost(post);
        return R.ok("帖子发表成功", postId);
    }

    /**
     * 更新帖子
     */
    @PutMapping("/{postId}")
    public R<Void> updatePost(@PathVariable Long postId, @RequestBody GroupPost post) {
        User currentUser = getCurrentUser();
        post.setPostId(postId);
        post.setAuthorId(currentUser.getUserId());
        
        boolean success = groupPostService.updatePost(post);
        return success ? R.ok("帖子更新成功", null) : R.fail("更新失败，请确认您是帖子作者");
    }

    /**
     * 删除帖子
     */
    @DeleteMapping("/{postId}")
    public R<Void> deletePost(@PathVariable Long postId) {
        User currentUser = getCurrentUser();
        boolean success = groupPostService.deletePost(postId, currentUser.getUserId());
        
        return success ? R.ok("帖子删除成功", null) : R.fail("删除失败，请确认您有权限删除该帖子");
    }

    /**
     * 点赞帖子
     */
    @PostMapping("/{postId}/like")
    public R<Void> likePost(@PathVariable Long postId) {
        User currentUser = getCurrentUser();
        boolean success = groupPostService.likePost(postId, currentUser.getUserId());
        
        return success ? R.ok("点赞成功", null) : R.fail("点赞失败");
    }

    /**
     * 取消点赞
     */
    @PostMapping("/{postId}/unlike")
    public R<Void> unlikePost(@PathVariable Long postId) {
        User currentUser = getCurrentUser();
        boolean success = groupPostService.unlikePost(postId, currentUser.getUserId());
        
        return success ? R.ok("已取消点赞", null) : R.fail("取消点赞失败");
    }
}
