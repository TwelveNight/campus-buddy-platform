package com.example.campusbuddy.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "用户信息展示对象")
public class UserVO {
    private Long userId;
    private String username;
    private String nickname;
    private String avatarUrl;
    private String major;
    private String grade;
    private Integer creditScore;
}
