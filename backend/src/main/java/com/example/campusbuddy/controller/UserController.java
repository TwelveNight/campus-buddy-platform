package com.example.campusbuddy.controller;

import com.example.campusbuddy.entity.User;
import com.example.campusbuddy.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/user")
public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping("/list")
    public List<User> listUsers() {
        return userService.list();
    }
}
