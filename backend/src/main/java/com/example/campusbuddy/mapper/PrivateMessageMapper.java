package com.example.campusbuddy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.campusbuddy.entity.PrivateMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 私信Mapper接口
 */
@Mapper
public interface PrivateMessageMapper extends BaseMapper<PrivateMessage> {

    /**
     * 获取两用户之间的聊天记录
     */
    @Select("SELECT * FROM private_message WHERE (sender_id = #{userId1} AND recipient_id = #{userId2}) OR (sender_id = #{userId2} AND recipient_id = #{userId1}) ORDER BY created_at ASC")
    List<PrivateMessage> getChatHistory(@Param("userId1") Long userId1, @Param("userId2") Long userId2);

    /**
     * 获取用户未读私信数量
     */
    @Select("SELECT COUNT(*) FROM private_message WHERE recipient_id = #{userId} AND is_read = 0")
    int countUnreadByUserId(@Param("userId") Long userId);

    /**
     * 标记用户与特定用户之间的所有私信为已读
     */
    @Update("UPDATE private_message SET is_read = 1 WHERE recipient_id = #{recipientId} AND sender_id = #{senderId} AND is_read = 0")
    int markAllAsReadBySender(@Param("recipientId") Long recipientId, @Param("senderId") Long senderId);

    /**
     * 标记指定私信为已读
     */
    @Update("UPDATE private_message SET is_read = 1 WHERE message_id = #{messageId}")
    int markAsRead(@Param("messageId") Long messageId);
}
