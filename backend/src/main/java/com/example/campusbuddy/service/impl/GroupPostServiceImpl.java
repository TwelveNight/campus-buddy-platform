package com.example.campusbuddy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.campusbuddy.entity.Group;
import com.example.campusbuddy.entity.GroupMember;
import com.example.campusbuddy.entity.GroupPost;
import com.example.campusbuddy.entity.User;
import com.example.campusbuddy.mapper.GroupMapper;
import com.example.campusbuddy.mapper.GroupMemberMapper;
import com.example.campusbuddy.mapper.GroupPostMapper;
import com.example.campusbuddy.service.GroupPostService;
import com.example.campusbuddy.service.PostLikeService;
import com.example.campusbuddy.service.UserService;
import com.example.campusbuddy.vo.GroupPostVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GroupPostServiceImpl extends ServiceImpl<GroupPostMapper, GroupPost> implements GroupPostService {

    @Autowired
    private GroupMemberMapper groupMemberMapper;
    
    @Autowired
    private GroupMapper groupMapper;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private PostLikeService postLikeService;

    @Override
    public IPage<GroupPost> queryGroupPosts(Long groupId, Integer pageNum, Integer pageSize) {
        // 构建查询条件
        LambdaQueryWrapper<GroupPost> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(GroupPost::getGroupId, groupId)
                .eq(GroupPost::getStatus, "PUBLISHED")
                .orderByDesc(GroupPost::getCreatedAt);
        
        // 执行分页查询
        return page(new Page<>(pageNum, pageSize), queryWrapper);
    }

    @Override
    @Transactional
    public Long createPost(GroupPost post) {
        // 设置初始值
        post.setLikeCount(0);
        post.setCommentCount(0);
        post.setStatus("PUBLISHED");
        post.setCreatedAt(new Date());
        post.setUpdatedAt(new Date());
        
        // 保存帖子
        save(post);
        
        // 更新小组帖子计数
        Group group = groupMapper.selectById(post.getGroupId());
        if (group != null) {
            group.setPostCount(group.getPostCount() + 1);
            group.setUpdatedAt(new Date());
            groupMapper.updateById(group);
        }
        
        return post.getPostId();
    }

    @Override
    public boolean updatePost(GroupPost post) {
        // 检查用户是否有权限更新
        GroupPost original = getById(post.getPostId());
        if (original == null || !original.getAuthorId().equals(post.getAuthorId())) {
            return false; // 非作者无权更新
        }
        
        post.setUpdatedAt(new Date());
        return updateById(post);
    }

    @Override
    @Transactional
    public boolean deletePost(Long postId, Long userId) {
        // 获取帖子信息
        GroupPost post = getById(postId);
        if (post == null) {
            return false;
        }
        
        // 检查是否是作者或管理员
        boolean isAuthor = post.getAuthorId().equals(userId);
        
        if (!isAuthor) {
            // 检查是否是小组管理员或创建者
            LambdaQueryWrapper<GroupMember> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(GroupMember::getGroupId, post.getGroupId())
                    .eq(GroupMember::getUserId, userId)
                    .in(GroupMember::getRole, "ADMIN", "CREATOR");
            
            if (groupMemberMapper.selectCount(queryWrapper) == 0) {
                return false; // 既不是作者也不是管理员，无权删除
            }
        }
        
        // 逻辑删除帖子
        post.setStatus("DELETED");
        post.setUpdatedAt(new Date());
        boolean result = updateById(post);
        
        // 更新小组帖子计数
        if (result) {
            Group group = groupMapper.selectById(post.getGroupId());
            if (group != null && group.getPostCount() > 0) {
                group.setPostCount(group.getPostCount() - 1);
                group.setUpdatedAt(new Date());
                groupMapper.updateById(group);
            }
        }
        
        return result;
    }

    @Override
    public GroupPost getPostDetail(Long postId) {
        return getById(postId);
    }
    
    @Override
    public boolean likePost(Long postId, Long userId) {
        // 检查帖子是否存在
        GroupPost post = getById(postId);
        if (post == null) {
            return false;
        }
        
        // 检查用户是否已经点赞过
        if (postLikeService.isLiked(postId, userId)) {
            return false; // 已经点赞过，不能重复点赞
        }
        
        // 添加点赞记录
        boolean success = postLikeService.addLike(postId, userId);
        if (success) {
            // 更新帖子的点赞数
            post.setLikeCount(post.getLikeCount() + 1);
            post.setUpdatedAt(new Date());
            updateById(post);
        }
        return success;
    }

    @Override
    public boolean unlikePost(Long postId, Long userId) {
        // 检查帖子是否存在
        GroupPost post = getById(postId);
        if (post == null || post.getLikeCount() <= 0) {
            return false;
        }
        
        // 检查用户是否点赞过
        if (!postLikeService.isLiked(postId, userId)) {
            return false; // 没有点赞过，不能取消点赞
        }
        
        // 移除点赞记录
        boolean success = postLikeService.removeLike(postId, userId);
        if (success) {
            // 更新帖子的点赞数
            post.setLikeCount(post.getLikeCount() - 1);
            post.setUpdatedAt(new Date());
            updateById(post);
        }
        return success;
    }

    @Override
    public void increaseCommentCount(Long postId) {
        GroupPost post = getById(postId);
        if (post != null) {
            int count = post.getCommentCount() == null ? 0 : post.getCommentCount();
            post.setCommentCount(count + 1);
            post.setUpdatedAt(new Date());
            updateById(post);
        }
    }

    @Override
    public void decreaseCommentCount(Long postId) {
        GroupPost post = getById(postId);
        if (post != null && post.getCommentCount() != null && post.getCommentCount() > 0) {
            post.setCommentCount(post.getCommentCount() - 1);
            post.setUpdatedAt(new Date());
            updateById(post);
        }
    }
    
    // =============== 管理员方法实现 ===============
    @Override
    public Page<GroupPost> adminPagePosts(Integer page, Integer size, String keyword, Long groupId, String status) {
        Page<GroupPost> pageInfo = new Page<>(page, size);
        LambdaQueryWrapper<GroupPost> queryWrapper = new LambdaQueryWrapper<>();
        
        // 添加关键词搜索条件
        if (keyword != null && !keyword.trim().isEmpty()) {
            queryWrapper.like(GroupPost::getTitle, keyword)
                    .or()
                    .like(GroupPost::getContent, keyword);
        }
        
        // 添加小组ID过滤条件
        if (groupId != null) {
            queryWrapper.eq(GroupPost::getGroupId, groupId);
        }
        
        // 添加状态过滤条件
        if (status != null && !status.trim().isEmpty()) {
            queryWrapper.eq(GroupPost::getStatus, status);
        }
        
        // 按创建时间倒序排序
        queryWrapper.orderByDesc(GroupPost::getCreatedAt);
        
        return page(pageInfo, queryWrapper);
    }
    
    @Override
    public Page<GroupPostVO> adminPagePostVOs(Integer page, Integer size, String keyword, Long groupId, String status) {
        // 先获取原始的 GroupPost 分页数据
        Page<GroupPost> postPage = adminPagePosts(page, size, keyword, groupId, status);
        
        // 创建一个新的 GroupPostVO 分页对象
        Page<GroupPostVO> voPage = new Page<>(postPage.getCurrent(), postPage.getSize(), postPage.getTotal());
        
        // 将 GroupPost 转换为 GroupPostVO
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
            
            // 查询作者信息
            User author = userService.getById(post.getAuthorId());
            if (author != null) {
                vo.setAuthorName(author.getNickname() != null ? author.getNickname() : author.getUsername());
                vo.setAuthorAvatar(author.getAvatarUrl());
            } else {
                vo.setAuthorName("未知用户");
            }
            
            // 查询小组信息
            Group group = groupMapper.selectById(post.getGroupId());
            if (group != null) {
                vo.setGroupName(group.getName());
                vo.setGroupAvatar(group.getAvatarUrl());
            } else {
                vo.setGroupName("未知小组");
            }
            
            return vo;
        }).collect(Collectors.toList());
        
        voPage.setRecords(voList);
        return voPage;
    }
    
    @Override
    @Transactional
    public boolean adminUpdatePostStatus(Long postId, String status) {
        GroupPost post = getById(postId);
        if (post == null) {
            return false;
        }
        
        post.setStatus(status);
        post.setUpdatedAt(new Date());
        
        return updateById(post);
    }
    
    @Override
    @Transactional
    public boolean adminDeletePost(Long postId) {
        return removeById(postId);
    }
}
