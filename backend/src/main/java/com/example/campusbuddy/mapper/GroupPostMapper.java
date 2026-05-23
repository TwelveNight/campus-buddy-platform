package com.example.campusbuddy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.campusbuddy.entity.GroupPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface GroupPostMapper extends BaseMapper<GroupPost> {
    @Update("UPDATE group_post SET like_count = like_count + 1, updated_at = NOW() WHERE post_id = #{postId}")
    int incrementLikeCount(@Param("postId") Long postId);

    @Update("UPDATE group_post SET like_count = GREATEST(COALESCE(like_count, 0) - 1, 0), updated_at = NOW() WHERE post_id = #{postId}")
    int decrementLikeCount(@Param("postId") Long postId);
}
