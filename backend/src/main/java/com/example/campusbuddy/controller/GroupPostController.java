package com.example.campusbuddy.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.campusbuddy.common.R;
import com.example.campusbuddy.entity.GroupPost;
import com.example.campusbuddy.entity.PostComment;
import com.example.campusbuddy.entity.User;
import com.example.campusbuddy.service.GroupPostCacheService;
import com.example.campusbuddy.service.GroupPostService;
import com.example.campusbuddy.service.PostCommentService;
import com.example.campusbuddy.service.PostLikeService;
import com.example.campusbuddy.service.UserService;
import com.example.campusbuddy.vo.GroupPostVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Tag(name = "学习小组帖子接口", description = "学习小组讨论区帖子相关操作")
@RestController
@RequestMapping("/api/group-posts")
public class GroupPostController {

    @Autowired
    private GroupPostService groupPostService;

    @Autowired
    private UserService userService;
    
    @Autowired
    private PostLikeService postLikeService;
    
    @Autowired
    private PostCommentService commentService;
    
    @Autowired
    private GroupPostCacheService postCacheService;

    private static final Logger log = LoggerFactory.getLogger(GroupPostController.class);
    
    // 缓存过期时间（秒）
    private static final long POSTS_CACHE_EXPIRE = 1800; // 30分钟
    private static final long POST_DETAIL_CACHE_EXPIRE = 3600; // 1小时
    private static final long USER_CACHE_EXPIRE = 3600; // 1小时
    private static final long HOT_POSTS_CACHE_EXPIRE = 1800; // 30分钟

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
    public R<IPage<GroupPostVO>> getGroupPosts(
            @Parameter(description = "小组ID") @RequestParam Long groupId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer pageSize) {

        // 先从缓存获取
        IPage<GroupPostVO> cachedPosts = postCacheService.getCachedGroupPosts(groupId, pageNum, pageSize);
        if (cachedPosts != null) {
            log.info("从缓存获取小组帖子列表成功: groupId={}, pageNum={}, pageSize={}", groupId, pageNum, pageSize);
            return R.ok(cachedPosts);
        }

        // 缓存未命中，从数据库获取
        IPage<GroupPost> posts = groupPostService.queryGroupPosts(groupId, pageNum, pageSize);
        
        // 提取所有作者ID，进行批量查询
        List<Long> authorIds = posts.getRecords().stream()
                .map(GroupPost::getAuthorId)
                .distinct()
                .collect(Collectors.toList());
        
        // 先从缓存获取用户信息
        Map<Long, User> userMap = postCacheService.getBatchCachedUsers(authorIds);
        
        // 对于缓存未命中的用户，从数据库查询
        List<Long> missingUserIds = authorIds.stream()
                .filter(id -> !userMap.containsKey(id))
                .collect(Collectors.toList());
        
        if (!missingUserIds.isEmpty()) {
            List<User> users = userService.listByIds(missingUserIds);
            for (User user : users) {
                userMap.put(user.getUserId(), user);
            }
            
            // 将新查询的用户信息加入缓存
            postCacheService.batchCacheUsers(userMap, USER_CACHE_EXPIRE);
        }
        
        // 构建VO对象
        List<GroupPostVO> voList = posts.getRecords().stream().map(post -> {
            GroupPostVO vo = new GroupPostVO();
            vo.postId = post.getPostId();
            vo.groupId = post.getGroupId();
            vo.authorId = post.getAuthorId();
            vo.title = post.getTitle();
            vo.content = post.getContent();
            vo.contentType = post.getContentType();
            vo.likeCount = post.getLikeCount();
            vo.commentCount = post.getCommentCount();
            vo.status = post.getStatus();
            vo.createdAt = post.getCreatedAt();
            vo.updatedAt = post.getUpdatedAt();
            
            // 从用户Map中获取作者信息
            User user = userMap.get(post.getAuthorId());
            vo.authorName = user != null ? (user.getNickname() != null ? user.getNickname() : user.getUsername()) : "未知用户";
            vo.authorAvatar = user != null ? user.getAvatarUrl() : null;
            return vo;
        }).collect(Collectors.toList());
        
        IPage<GroupPostVO> voPage = posts.convert(post -> voList.stream()
                .filter(vo -> vo.postId.equals(post.getPostId()))
                .findFirst()
                .orElse(null));
        
        // 将结果存入缓存
        postCacheService.cacheGroupPosts(groupId, pageNum, pageSize, voPage, POSTS_CACHE_EXPIRE);
        
        return R.ok(voPage);
    }

    /**
     * 获取帖子详情
     */
    @Operation(summary = "获取帖子详情")
    @GetMapping("/{postId}")
    public R<GroupPostVO> getPostDetail(@Parameter(description = "帖子ID") @PathVariable Long postId) {
        // 先从缓存获取
        GroupPostVO cachedPost = postCacheService.getCachedPostDetail(postId);
        if (cachedPost != null) {
            log.info("从缓存获取帖子详情成功: postId={}", postId);
            return R.ok(cachedPost);
        }
        
        // 缓存未命中，从数据库获取
        GroupPost post = groupPostService.getPostDetail(postId);
        if (post == null) {
            return R.fail("帖子不存在");
        }
        
        GroupPostVO vo = new GroupPostVO();
        vo.postId = post.getPostId();
        vo.groupId = post.getGroupId();
        vo.authorId = post.getAuthorId();
        vo.title = post.getTitle();
        vo.content = post.getContent();
        vo.contentType = post.getContentType();
        vo.likeCount = post.getLikeCount();
        vo.commentCount = post.getCommentCount();
        vo.status = post.getStatus();
        vo.createdAt = post.getCreatedAt();
        vo.updatedAt = post.getUpdatedAt();
        
        // 先从缓存获取用户信息
        User user = postCacheService.getCachedUser(post.getAuthorId());
        
        // 缓存未命中，从数据库获取
        if (user == null) {
            user = userService.getById(post.getAuthorId());
            // 将用户信息存入缓存
            if (user != null) {
                Map<Long, User> userMap = new HashMap<>();
                userMap.put(user.getUserId(), user);
                postCacheService.batchCacheUsers(userMap, USER_CACHE_EXPIRE);
            }
        }
        
        vo.authorName = user != null ? (user.getNickname() != null ? user.getNickname() : user.getUsername()) : "未知用户";
        vo.authorAvatar = user != null ? user.getAvatarUrl() : null;
        
        // 将帖子详情存入缓存
        postCacheService.cachePostDetail(postId, vo, POST_DETAIL_CACHE_EXPIRE);
        
        return R.ok(vo);
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
        
        // 清除小组帖子列表缓存
        postCacheService.evictGroupPostsCache(post.getGroupId());
        
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
        
        if (success) {
            // 清除帖子详情缓存
            postCacheService.evictPostDetailCache(postId);
            // 清除小组帖子列表缓存
            postCacheService.evictGroupPostsCache(post.getGroupId());
        }
        
        return success ? R.ok("帖子更新成功", null) : R.fail("更新失败，请确认您是帖子作者");
    }

    /**
     * 删除帖子
     */
    @Operation(summary = "删除帖子", description = "仅作者可删除帖子")
    @DeleteMapping("/{postId}")
    public R<Void> deletePost(@Parameter(description = "帖子ID") @PathVariable Long postId) {
        // 先获取帖子信息，用于之后清除缓存
        GroupPost post = groupPostService.getById(postId);
        Long groupId = post != null ? post.getGroupId() : null;
        
        User currentUser = getCurrentUser();
        boolean success = groupPostService.deletePost(postId, currentUser.getUserId());
        
        if (success && groupId != null) {
            // 清除帖子详情缓存
            postCacheService.evictPostDetailCache(postId);
            // 清除小组帖子列表缓存
            postCacheService.evictGroupPostsCache(groupId);
            // 清除热门帖子缓存
            postCacheService.evictHotPostsCache();
        }
        
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
        
        return success ? 
            R.ok("点赞成功", null) : 
            R.fail("点赞失败，可能您已经点过赞或帖子不存在");
    }

    /**
     * 取消点赞
     */
    @Operation(summary = "取消点赞")
    @PostMapping("/{postId}/unlike")
    public R<Void> unlikePost(@Parameter(description = "帖子ID") @PathVariable Long postId) {
        User currentUser = getCurrentUser();
        boolean success = groupPostService.unlikePost(postId, currentUser.getUserId());
        
        return success ? 
            R.ok("已取消点赞", null) : 
            R.fail("取消点赞失败，可能您未曾点赞或帖子不存在");
    }

    /**
     * 获取用户对帖子的点赞状态
     */
    @Operation(summary = "获取点赞状态", description = "检查当前用户是否已点赞该帖子")
    @GetMapping("/{postId}/like-status")
    public R<Boolean> getLikeStatus(@Parameter(description = "帖子ID") @PathVariable Long postId) {
        User currentUser = getCurrentUser();
        boolean isLiked = postLikeService.isLiked(postId, currentUser.getUserId());
        
        return R.ok(isLiked);
    }
    
    /**
     * 获取帖子评论列表
     */
    @Operation(summary = "获取帖子评论列表", description = "分页获取指定帖子的评论")
    @GetMapping("/{postId}/comments")
    public R<Map<String, Object>> getPostComments(
            @Parameter(description = "帖子ID") @PathVariable Long postId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer pageSize) {

        IPage<PostComment> commentsPage = commentService.getPostComments(postId, pageNum, pageSize);
        List<PostComment> comments = commentsPage.getRecords();
        
        // 获取评论用户信息
        List<Long> userIds = comments.stream().map(PostComment::getUserId).collect(Collectors.toList());
        Map<Long, User> userMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            userService.listByIds(userIds).forEach(user -> userMap.put(user.getUserId(), user));
        }
        
        // 组装返回数据
        List<Map<String, Object>> commentsList = comments.stream().map(comment -> {
            Map<String, Object> map = new HashMap<>();
            map.put("commentId", comment.getCommentId());
            map.put("postId", comment.getPostId());
            map.put("content", comment.getContent());
            map.put("parentId", comment.getParentId());
            map.put("createdAt", comment.getCreatedAt());
            
            // 添加用户信息
            User user = userMap.get(comment.getUserId());
            if (user != null) {
                map.put("userId", user.getUserId());
                map.put("username", user.getUsername());
                map.put("nickname", user.getNickname());
                map.put("avatar", user.getAvatarUrl());
            }
            
            return map;
        }).collect(Collectors.toList());
        
        // 返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("comments", commentsList);
        result.put("total", commentsPage.getTotal());
        result.put("pageNum", commentsPage.getCurrent());
        result.put("pageSize", commentsPage.getSize());
        
        return R.ok(result);
    }

    /**
     * 添加评论
     */
    @Operation(summary = "添加评论", description = "为帖子添加评论")
    @PostMapping("/{postId}/comments")
    public R<Long> addComment(
            @Parameter(description = "帖子ID") @PathVariable Long postId,
            @RequestBody Map<String, String> commentData) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return R.fail("用户未登录");
        }
        
        String content = commentData.get("content");
        if (content == null || content.trim().isEmpty()) {
            return R.fail("评论内容不能为空");
        }
        
        PostComment comment = new PostComment();
        comment.setPostId(postId);
        comment.setUserId(currentUser.getUserId());
        comment.setContent(content.trim());
        
        Long commentId = commentService.addComment(comment);
        
        // 新增：更新帖子评论数
        groupPostService.increaseCommentCount(postId);
        
        return R.ok(commentId);
    }

    /**
     * 删除评论
     */
    @Operation(summary = "删除评论", description = "删除自己的评论")
    @DeleteMapping("/{postId}/comments/{commentId}")
    public R<Void> deleteComment(
            @Parameter(description = "帖子ID") @PathVariable Long postId,
            @Parameter(description = "评论ID") @PathVariable Long commentId) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return R.fail("用户未登录");
        }
        
        boolean success = commentService.deleteComment(commentId, currentUser.getUserId());
        
        if (success) {
            // 新增：减少帖子评论数
            groupPostService.decreaseCommentCount(postId);
        }
        
        return success ? R.ok("删除成功", null) : R.fail("删除失败，可能没有权限或评论不存在");
    }

    /**
     * 获取评论详情
     */
    @Operation(summary = "获取评论详情")
    @GetMapping("/{postId}/comments/{commentId}")
    public R<PostComment> getCommentDetail(
            @Parameter(description = "帖子ID") @PathVariable Long postId,
            @Parameter(description = "评论ID") @PathVariable Long commentId) {
        PostComment comment = commentService.getCommentDetail(commentId);
        if (comment == null) {
            return R.fail("评论不存在");
        }
        
        // 验证评论是否属于该帖子
        if (!comment.getPostId().equals(postId)) {
            return R.fail("评论不属于该帖子");
        }
        
        return R.ok(comment);
    }
}
