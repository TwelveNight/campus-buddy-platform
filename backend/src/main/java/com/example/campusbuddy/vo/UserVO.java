package com.example.campusbuddy.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.Date;
import java.util.List;

@Data
@Schema(description = "用户信息展示对象")
public class UserVO {
    private Long userId;
    private String username;
    private String nickname;
    private String avatarUrl;
    private String gender; // 性别: MALE, FEMALE, UNKNOWN
    private String major;
    private String grade;
    private String contactInfo; // 联系方式
    private String skillTags; // 技能标签(JSON字符串)
    private Integer creditScore;
    private String status; // 账号状态: ACTIVE, INACTIVE, BANNED
    private Date createdAt; // 创建时间
    private Date updatedAt; // 更新时间
    
    @Schema(description = "用户角色列表")
    private List<String> roles; // 用户角色列表
}
