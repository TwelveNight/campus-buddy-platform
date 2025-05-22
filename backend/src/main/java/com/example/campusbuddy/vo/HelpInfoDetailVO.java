package com.example.campusbuddy.vo;

import com.example.campusbuddy.entity.HelpInfo;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 互助信息详情VO，包含发布者基本信息
 */
@Data
public class HelpInfoDetailVO {
    private Long infoId;
    private Long publisherId;
    private String publisherName;
    private String publisherAvatar;
    private String type;
    private String title;
    private String description;
    private String expectedTime;
    private String expectedLocation;
    private String contactMethod;
    private BigDecimal rewardAmount;
    private String imageUrls;
    private String status;
    private Integer viewCount;
    private Long acceptedApplicationId; // 已接受的申请ID
    private Date createdAt;
    private Date updatedAt;

    // 评价状态相关字段
    private Long helperId; // 帮助者ID
    private String helperName; // 帮助者姓名
    private Boolean canPublisherReview; // 发布者是否可以评价
    private Boolean canHelperReview; // 帮助者是否可以评价
    private Boolean publisherHasReviewed; // 发布者是否已评价
    private Boolean helperHasReviewed; // 帮助者是否已评价

    // 从HelpInfo实体转换为VO
    public static HelpInfoDetailVO fromEntity(HelpInfo helpInfo) {
        if (helpInfo == null) {
            return null;
        }

        HelpInfoDetailVO vo = new HelpInfoDetailVO();
        vo.setInfoId(helpInfo.getInfoId());
        vo.setPublisherId(helpInfo.getPublisherId());
        vo.setType(helpInfo.getType());
        vo.setTitle(helpInfo.getTitle());
        vo.setDescription(helpInfo.getDescription());
        vo.setExpectedTime(helpInfo.getExpectedTime());
        vo.setExpectedLocation(helpInfo.getExpectedLocation());
        vo.setContactMethod(helpInfo.getContactMethod());
        vo.setRewardAmount(helpInfo.getRewardAmount());
        vo.setImageUrls(helpInfo.getImageUrls());
        vo.setStatus(helpInfo.getStatus());
        vo.setViewCount(helpInfo.getViewCount());
        vo.setAcceptedApplicationId(helpInfo.getAcceptedApplicationId());
        vo.setCreatedAt(helpInfo.getCreatedAt());
        vo.setUpdatedAt(helpInfo.getUpdatedAt());

        return vo;
    }
}
