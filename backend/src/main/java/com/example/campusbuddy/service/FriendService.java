package com.example.campusbuddy.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.campusbuddy.dto.FriendRequestDTO;
import com.example.campusbuddy.entity.Friend;
import com.example.campusbuddy.vo.FriendRequestVO;
import com.example.campusbuddy.vo.FriendVO;
import com.example.campusbuddy.vo.UserVO;

import java.util.List;

/**
 * 好友服务接口
 */
public interface FriendService extends IService<Friend> {
    
    /**
     * 发送好友申请
     * 
     * @param requesterId 申请者ID
     * @param dto 申请信息
     * @return 申请ID
     */
    Long applyFriend(Long requesterId, FriendRequestDTO dto);
    
    /**
     * 接受好友申请
     * 
     * @param userId 当前用户ID
     * @param requestId 申请ID
     * @return 是否成功
     */
    boolean acceptFriendRequest(Long userId, Long requestId);
    
    /**
     * 拒绝好友申请
     * 
     * @param userId 当前用户ID
     * @param requestId 申请ID
     * @return 是否成功
     */
    boolean rejectFriendRequest(Long userId, Long requestId);
    
    /**
     * 获取用户的好友列表
     * 
     * @param userId 用户ID
     * @return 好友列表
     */
    List<FriendVO> getFriendList(Long userId);
    
    /**
     * 获取用户的好友申请列表
     * 
     * @param userId 用户ID
     * @return 申请列表
     */
    List<FriendRequestVO> getFriendRequests(Long userId);
    
    /**
     * 删除好友
     * 
     * @param userId 当前用户ID
     * @param friendId 好友ID
     * @return 是否成功
     */
    boolean deleteFriend(Long userId, Long friendId);
    
    /**
     * 搜索好友
     * 
     * @param userId 用户ID
     * @param keyword 关键词
     * @param page 页码
     * @param size 每页大小
     * @return 好友列表
     */
    Page<FriendVO> searchFriends(Long userId, String keyword, Integer page, Integer size);
    
    /**
     * 检查是否已经是好友
     * 
     * @param userId 用户ID
     * @param friendId 好友ID
     * @return 是否是好友
     */
    boolean isFriend(Long userId, Long friendId);
    
    /**
     * 获取好友IDs列表
     * 
     * @param userId 用户ID
     * @return 好友ID列表
     */
    List<Long> getFriendIds(Long userId);
    
    /**
     * 获取好友申请状态
     * 
     * @param userId 用户ID
     * @param otherUserId 对方用户ID
     * @return 申请状态: null-无申请, "PENDING"-待处理, "ACCEPTED"-已接受, "REJECTED"-已拒绝
     */
    String getFriendRequestStatus(Long userId, Long otherUserId);
}
