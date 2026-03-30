package com.example.campusbuddy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.campusbuddy.entity.UserRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserRoleMapper extends BaseMapper<UserRole> {

    /**
     * 根据用户ID获取角色列表
     */
    @Select("SELECT role_name FROM user_role WHERE user_id = #{userId}")
    List<String> findRolesByUserId(@Param("userId") Long userId);

    /**
     * 统计用户拥有某角色的记录数
     */
    @Select("SELECT COUNT(*) FROM user_role WHERE user_id = #{userId} AND role_name = #{roleName}")
    long countByUserIdAndRoleName(@Param("userId") Long userId, @Param("roleName") String roleName);

    /**
     * 查询用户的特定角色记录
     */
    @Select("SELECT * FROM user_role WHERE user_id = #{userId} AND role_name = #{roleName} LIMIT 1")
    UserRole findByUserIdAndRoleName(@Param("userId") Long userId, @Param("roleName") String roleName);
}
