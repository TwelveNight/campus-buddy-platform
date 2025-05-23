package com.example.campusbuddy.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "用户个人信息更新DTO")
public class ProfileUpdateDTO {
    @Schema(description = "用户昵称")
    private String nickname;

    @Schema(description = "用户头像URL")
    private String avatarUrl;
    
    @Schema(description = "性别(MALE, FEMALE, UNKNOWN)")
    private String gender;
    
    @Schema(description = "专业")
    private String major;
    
    @Schema(description = "年级")
    private String grade;
    
    @Schema(description = "联系方式")
    private String contactInfo;
    
    @Schema(description = "技能标签(JSON字符串)")
    private String skillTags;
}
