package com.example.campusbuddy.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.campusbuddy.entity.FriendRequest;
import com.example.campusbuddy.vo.FriendRequestVO;

import java.util.List;

/**
 * 好友申请服务接口
 */
public interface FriendRequestService extends IService<FriendRequest> {
    
    /**
     * 构建FriendRequestVO
     * 
     * @param request 好友申请实体
     * @return FriendRequestVO
     */
    FriendRequestVO buildRequestVO(FriendRequest request);
    
    /**
     * 批量构建FriendRequestVO
     * 
     * @param requests 好友申请实体列表
     * @return FriendRequestVO列表
     */
    List<FriendRequestVO> buildRequestVOList(List<FriendRequest> requests);
}
