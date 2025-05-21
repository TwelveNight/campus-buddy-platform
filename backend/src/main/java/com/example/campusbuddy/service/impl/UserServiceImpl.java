package com.example.campusbuddy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.campusbuddy.dto.LoginDTO;
import com.example.campusbuddy.dto.RegisterDTO;
import com.example.campusbuddy.entity.User;
import com.example.campusbuddy.mapper.UserMapper;
import com.example.campusbuddy.security.JwtUtil;
import com.example.campusbuddy.service.UserService;
import com.example.campusbuddy.vo.UserVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private UserMapper userMapper;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public UserVO register(RegisterDTO dto) {
        // 检查用户名是否已存在
        if (userMapper.selectOne(new QueryWrapper<User>().eq("username", dto.getUsername())) != null) {
            throw new IllegalArgumentException("用户名已存在");
        }
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        user.setNickname(dto.getNickname());
        user.setCreditScore(100);
        user.setStatus("ACTIVE");
        this.save(user);
        return getUserVOById(user.getUserId());
    }

    @Override
    public String login(LoginDTO dto) {
        User user = userMapper.selectOne(new QueryWrapper<User>().eq("username", dto.getUsername()));
        if (user == null || !passwordEncoder.matches(dto.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("用户名或密码错误");
        }
        return jwtUtil.generateToken(user.getUserId(), user.getUsername());
    }

    @Override
    public UserVO getUserVOById(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null)
            return null;
        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);
        return vo;
    }
}
