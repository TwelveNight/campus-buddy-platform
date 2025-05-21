package com.example.campusbuddy.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.campusbuddy.dto.LoginDTO;
import com.example.campusbuddy.dto.RegisterDTO;
import com.example.campusbuddy.entity.User;
import com.example.campusbuddy.vo.UserVO;

public interface UserService extends IService<User> {
    UserVO register(RegisterDTO dto);
    String login(LoginDTO dto);
    UserVO getUserVOById(Long userId);
}
