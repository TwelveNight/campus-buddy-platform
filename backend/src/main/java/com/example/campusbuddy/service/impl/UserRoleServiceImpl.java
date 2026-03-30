package com.example.campusbuddy.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.campusbuddy.entity.UserRole;
import com.example.campusbuddy.mapper.UserRoleMapper;
import com.example.campusbuddy.service.UserRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserRoleServiceImpl extends ServiceImpl<UserRoleMapper, UserRole> implements UserRoleService {

    @Autowired
    private UserRoleMapper userRoleMapper;

    @Override
    public UserRole addRoleToUser(Long userId, String roleName) {
        // 检查是否已经有该角色
        if (userRoleMapper.countByUserIdAndRoleName(userId, roleName) > 0) {
            // 已存在该角色，直接返回
            return userRoleMapper.findByUserIdAndRoleName(userId, roleName);
        }

        // 添加新角色
        UserRole userRole = new UserRole();
        userRole.setUserId(userId);
        userRole.setRoleName(roleName);
        this.save(userRole);
        return userRole;
    }

    @Override
    public List<String> getUserRoles(Long userId) {
        return userRoleMapper.findRolesByUserId(userId);
    }

    @Override
    public boolean hasRole(Long userId, String roleName) {
        return userRoleMapper.countByUserIdAndRoleName(userId, roleName) > 0;
    }
}
