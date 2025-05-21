package com.example.campusbuddy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
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
        QueryWrapper<UserRole> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId).eq("role_name", roleName);
        if (this.count(queryWrapper) > 0) {
            // 已存在该角色，直接返回
            return this.getOne(queryWrapper);
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
        QueryWrapper<UserRole> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId).eq("role_name", roleName);
        return this.count(queryWrapper) > 0;
    }
}
