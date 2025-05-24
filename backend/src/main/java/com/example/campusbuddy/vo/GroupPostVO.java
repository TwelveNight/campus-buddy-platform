package com.example.campusbuddy.vo;

import java.util.Date;

public class GroupPostVO {
    public Long postId;
    public Long groupId;
    public Long authorId;
    public String authorName;
    public String authorAvatar;
    public String title;
    public String content;
    public String contentType;
    public Integer likeCount;
    public Integer commentCount;
    public String status;
    public Date createdAt;
    public Date updatedAt;
}
