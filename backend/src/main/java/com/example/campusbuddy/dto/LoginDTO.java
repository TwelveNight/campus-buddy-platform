package com.example.campusbuddy.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "用户登录请求体")
public class LoginDTO {
    @Schema(description = "用户名/学号（密码登录时使用）")
    private String username;
    @Schema(description = "密码（密码登录时使用）")
    private String password;
    @Schema(description = "登录类型：PASSWORD（默认，用户名+密码 或 邮箱+密码）| CODE（邮箱+验证码）")
    private String loginType; // "PASSWORD" | "CODE"
    @Schema(description = "邮箱（邮箱登录时使用）")
    private String email;
    @Schema(description = "6位邮箱验证码（验证码登录时使用）")
    private String code;
}
