package com.example.campusbuddy.service;

import com.example.campusbuddy.entity.UserRole;
import java.util.List;

public interface UserRoleService {

    /**
     * 为用户添加角色
     * 
     * @param userId   用户ID
     * @param roleName 角色名称
     * @return 添加的角色信息
     */
    UserRole addRoleToUser(Long userId, String roleName);

    /**
     * 获取用户的所有角色
     * 
     * @param userId 用户ID
     * @return 角色名称列表
     */
    List<String> getUserRoles(Long userId);

    /**
     * 检查用户是否拥有指定角色
     * 
     * @param userId   用户ID
     * @param roleName 角色名称
     * @return 是否拥有角色
     */
    boolean hasRole(Long userId, String roleName);
}
