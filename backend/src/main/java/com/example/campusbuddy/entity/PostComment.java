package com.example.campusbuddy.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

@Data
@TableName("post_comment")
public class PostComment {
    @TableId(value = "comment_id", type = IdType.AUTO)
    private Long commentId;   // 评论ID，主键
    private Long postId;      // 帖子ID
    private Long userId;      // 用户ID
    private String content;   // 评论内容
    private Long parentId;    // 父评论ID，用于回复
    private String status;    // 状态：PUBLISHED、DELETED
    private Date createdAt;   // 创建时间
    private Date updatedAt;   // 更新时间
}
