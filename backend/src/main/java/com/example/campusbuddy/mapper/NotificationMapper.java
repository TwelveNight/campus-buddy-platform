package com.example.campusbuddy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.campusbuddy.entity.Notification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 消息通知Mapper接口
 */
@Mapper
public interface NotificationMapper extends BaseMapper<Notification> {

    /**
     * 获取用户未读通知数量
     */
    @Select("SELECT COUNT(*) FROM notification WHERE recipient_id = #{userId} AND is_read = 0")
    int countUnreadByUserId(@Param("userId") Long userId);

    /**
     * 标记用户所有通知为已读
     */
    @Update("UPDATE notification SET is_read = 1 WHERE recipient_id = #{userId} AND is_read = 0")
    int markAllAsRead(@Param("userId") Long userId);

    /**
     * 标记指定通知为已读
     */
    @Update("UPDATE notification SET is_read = 1 WHERE notification_id = #{notificationId}")
    int markAsRead(@Param("notificationId") Long notificationId);
}
