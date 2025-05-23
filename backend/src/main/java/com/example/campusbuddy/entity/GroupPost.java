package com.example.campusbuddy.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

@Data
@TableName("group_post")
public class GroupPost {
    @TableId(value = "post_id", type = IdType.AUTO)
    private Long postId;      // 帖子ID，主键
    private Long groupId;     // 小组ID
    private Long authorId;    // 作者ID
    private String title;     // 帖子标题
    private String content;   // 帖子内容
    private String contentType; // 内容类型：TEXT、HTML等
    private Integer likeCount; // 点赞数量
    private Integer commentCount; // 评论数量
    private String status;    // 状态：PUBLISHED、DELETED
    private Date createdAt;   // 创建时间
    private Date updatedAt;   // 更新时间
}
