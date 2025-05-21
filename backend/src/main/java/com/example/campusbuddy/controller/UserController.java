package com.example.campusbuddy.controller;

import com.example.campusbuddy.common.R;
import com.example.campusbuddy.dto.LoginDTO;
import com.example.campusbuddy.dto.RegisterDTO;
import com.example.campusbuddy.entity.User;
import com.example.campusbuddy.service.UserService;
import com.example.campusbuddy.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Tag(name = "用户接口", description = "用户相关操作")
@RestController
@RequestMapping("/api/user")
public class UserController {
    @Autowired
    private UserService userService;

    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public R<UserVO> register(@RequestBody RegisterDTO dto) {
        return R.ok(userService.register(dto));
    }

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public R<String> login(@RequestBody LoginDTO dto) {
        return R.ok(userService.login(dto));
    }

    @Operation(summary = "获取所有用户列表", description = "返回所有用户信息列表")
    @GetMapping("/list")
    public List<User> listUsers() {
        return userService.list();
    }
}
