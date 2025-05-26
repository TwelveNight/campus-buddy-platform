package com.example.campusbuddy.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.campusbuddy.dto.LoginDTO;
import com.example.campusbuddy.dto.PasswordUpdateDTO;
import com.example.campusbuddy.dto.ProfileUpdateDTO;
import com.example.campusbuddy.dto.RegisterDTO;
import com.example.campusbuddy.entity.User;
import com.example.campusbuddy.vo.UserVO;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.ArrayList;
import java.util.List;

public interface UserService extends IService<User> {
    UserVO register(RegisterDTO dto);

    String login(LoginDTO dto);

    UserVO getUserVOById(Long userId);

    UserVO updateProfile(Long userId, ProfileUpdateDTO dto);

    void updatePassword(Long userId, PasswordUpdateDTO dto);
    
    User getUserByUsername(String username);
    
    /**
     * 检查用户是否为管理员
     * 
     * @param userId 用户ID
     * @return 是否为管理员
     */
    boolean isAdmin(Long userId);
    
    /**
     * 检查用户是否存在
     * 
     * @param userId 用户ID
     * @return 是否存在
     */
    boolean exists(Long userId);
    
    /**
     * 根据关键词搜索用户
     * 
     * @param keyword 关键词(用户名、昵称或联系方式)
     * @param page 页码
     * @param size 每页大小
     * @return 用户分页结果
     */
    Page<UserVO> searchUsers(String keyword, Integer page, Integer size);

    com.baomidou.mybatisplus.extension.plugins.pagination.Page<UserVO> adminPageUsers(Integer page, Integer size, String keyword, String status);
    boolean adminUpdateUserStatus(Long userId, String status);
    String adminResetPassword(Long userId);
}
