package com.example.campusbuddy.vo;

import lombok.Data;
import java.util.Date;

@Data
public class ReviewVO {
    private Long reviewId;
    private Long reviewedUserId;
    private Long reviewerUserId;
    private String reviewerNickname;
    private String reviewerAvatar;
    private Long relatedInfoId;
    private String relatedInfoTitle; // 互助标题
    private String relatedInfoSummary; // 互助简介
    private Integer score;
    private String content;
    private String moduleType;
    private Date createdAt;
}
