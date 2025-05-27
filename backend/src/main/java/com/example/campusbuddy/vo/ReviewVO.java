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
    private String reviewedNickname; // 被评价者昵称
    private Long relatedInfoId;
    private String relatedInfoTitle; // 互助标题
    private String relatedInfoSummary; // 互助简介
    private Integer score;
    private String content;
    private String moduleType;
    private Date createdAt;
    private String reviewType; // PUBLISHER_TO_HELPER 或 HELPER_TO_PUBLISHER
    
    // 信用分相关字段
    private Integer reviewerCreditScore; // 评价者信用积分
    private String reviewerCreditLevel; // 评价者信用等级
}
