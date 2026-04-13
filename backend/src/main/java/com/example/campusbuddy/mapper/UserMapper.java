package com.example.campusbuddy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.campusbuddy.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    /** 根据用户名查询用户 */
    @Select("SELECT * FROM user WHERE username = #{username} LIMIT 1")
    User findByUsername(@Param("username") String username);

    /** 根据邮箱查询用户 */
    @Select("SELECT * FROM user WHERE email = #{email} LIMIT 1")
    User findByEmail(@Param("email") String email);

    /** 查询所有活跃用户 */
    @Select("SELECT * FROM user WHERE status = 'ACTIVE'")
    List<User> findAllActive();
}
