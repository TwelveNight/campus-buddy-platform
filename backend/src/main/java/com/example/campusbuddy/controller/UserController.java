package com.example.campusbuddy.controller;

import com.example.campusbuddy.common.R;
import com.example.campusbuddy.common.ResultCode;
import com.example.campusbuddy.dto.LoginDTO;
import com.example.campusbuddy.dto.RegisterDTO;
import com.example.campusbuddy.entity.User;
import com.example.campusbuddy.service.UserService;
import com.example.campusbuddy.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
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
        UserVO userVO = userService.register(dto);
        return R.ok("注册成功", userVO);
    }

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public R<String> login(@RequestBody LoginDTO dto) {
        String token = userService.login(dto);
        return R.ok("登录成功", token);
    }

    @Operation(summary = "获取所有用户列表", description = "返回所有用户信息列表")
    @GetMapping("/list")
    public R<List<User>> listUsers() {
        List<User> users = userService.list();
        return R.ok("获取用户列表成功", users);
    }

    @Operation(summary = "获取当前登录用户信息")
    @GetMapping("/me")
    public R<UserVO> getCurrentUser(jakarta.servlet.http.HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return R.fail(ResultCode.UNAUTHORIZED, "未登录");
        }
        UserVO userVO = userService.getUserVOById(userId);
        return R.ok("获取当前用户信息成功", userVO);
    }
}
