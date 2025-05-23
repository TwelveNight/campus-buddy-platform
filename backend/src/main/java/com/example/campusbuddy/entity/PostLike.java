package com.example.campusbuddy.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

@Data
@TableName("post_like")
public class PostLike {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;       // 主键ID
    private Long postId;   // 帖子ID
    private Long userId;   // 用户ID
    private Date createdAt; // 点赞时间
}
