package com.example.campusbuddy.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "发送邮箱验证码请求体")
public class EmailCodeDTO {
    @Schema(description = "目标邮箱", required = true)
    private String email;
    @Schema(description = "验证码用途：LOGIN（登录）| REGISTER（注册）", required = true)
    private String codeType;
}
