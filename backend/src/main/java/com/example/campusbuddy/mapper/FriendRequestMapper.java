package com.example.campusbuddy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.campusbuddy.entity.FriendRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 好友申请Mapper接口
 */
@Mapper
public interface FriendRequestMapper extends BaseMapper<FriendRequest> {
    
    /**
     * 获取好友申请列表（含申请者信息）
     * @param recipientId 接收者ID
     * @param status 申请状态
     * @return 申请信息列表
     */
    List<Map<String, Object>> getRequestsWithUserInfo(@Param("recipientId") Long recipientId, @Param("status") String status);
    
    /**
     * 获取用户发送的好友申请列表
     * @param requesterId 申请者ID
     * @param status 申请状态
     * @return 申请信息列表
     */
    List<Map<String, Object>> getSentRequestsWithUserInfo(@Param("requesterId") Long requesterId, @Param("status") String status);
}
