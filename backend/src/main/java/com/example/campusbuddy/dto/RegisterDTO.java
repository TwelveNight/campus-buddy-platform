package com.example.campusbuddy.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "用户注册请求体")
public class RegisterDTO {
    @Schema(description = "用户名/学号/邮箱", required = true)
    private String username;
    @Schema(description = "密码", required = true)
    private String password;
    @Schema(description = "昵称", required = true)
    private String nickname;
}
