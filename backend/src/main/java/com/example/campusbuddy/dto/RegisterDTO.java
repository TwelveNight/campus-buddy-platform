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
    @Schema(description = "邮箱（选填，填写后可使用邮箱登录）")
    private String email;
    @Schema(description = "邮箱验证码（填写邮箱时必填）")
    private String emailCode;
}
