package com.example.campusbuddy.vo;

import lombok.Data;
import java.util.Date;

@Data
public class GroupFileVO {
    private Long fileId;
    private Long groupId;
    private Long uploaderId;
    private String uploaderName;
    private String uploaderAvatar;
    private String fileName;
    private String fileType;
    private Long fileSize;
    private String fileUrl;
    private String description;
    private Integer downloadCount;
    private String status;
    private Date createdAt;   // 对应 entity 的 uploadedAt
    private Date updatedAt;
}
