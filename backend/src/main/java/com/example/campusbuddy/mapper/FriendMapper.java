package com.example.campusbuddy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.campusbuddy.entity.Friend;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 好友关系Mapper接口
 */
@Mapper
public interface FriendMapper extends BaseMapper<Friend> {
    
    /**
     * 查询用户好友ID列表
     * @param userId 用户ID
     * @return 好友ID列表
     */
    @Select("SELECT friend_id FROM friend WHERE user_id = #{userId}")
    List<Long> selectFriendIdsByUserId(@Param("userId") Long userId);
    
    /**
     * 获取好友列表（含用户信息）
     * @param userId 用户ID
     * @return 好友信息列表
     */
    List<Map<String, Object>> getFriendsWithUserInfo(@Param("userId") Long userId);
    
    /**
     * 搜索好友
     * @param userId 用户ID
     * @param keyword 关键词
     * @param offset 偏移量
     * @param limit 数量限制
     * @return 好友信息列表
     */
    List<Map<String, Object>> searchFriends(
            @Param("userId") Long userId, 
            @Param("keyword") String keyword, 
            @Param("offset") Integer offset, 
            @Param("limit") Integer limit);
    
    /**
     * 计算搜索好友的总数
     * @param userId 用户ID
     * @param keyword 关键词
     * @return 符合条件的好友数量
     */
    Integer countSearchFriends(@Param("userId") Long userId, @Param("keyword") String keyword);
}
