package com.example.campusbuddy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.campusbuddy.entity.Review;
import com.example.campusbuddy.vo.ReviewVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ReviewMapper extends BaseMapper<Review> {
    List<ReviewVO> selectReviewVOsForPage(@Param("userId") Long userId,
            @Param("type") String type,
            @Param("score") Integer score,
            @Param("moduleType") String moduleType,
            @Param("offset") int offset,
            @Param("size") int size);

    long countReviewVOsForPage(@Param("userId") Long userId,
            @Param("type") String type,
            @Param("score") Integer score,
            @Param("moduleType") String moduleType);

    /** 查询指定用户收到的所有评价 */
    @Select("SELECT * FROM review WHERE reviewed_user_id = #{userId}")
    List<Review> selectByReviewedUserId(@Param("userId") Long userId);

    /** 查询指定用户收到的所有评价（按创建时间降序） */
    @Select("SELECT * FROM review WHERE reviewed_user_id = #{userId} ORDER BY created_at DESC")
    List<Review> selectByReviewedUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);

    /** 统计自评价数量（reviewer = reviewed） */
    @Select("SELECT COUNT(*) FROM review WHERE reviewer_user_id = #{userId} AND reviewed_user_id = #{userId}")
    long countSelfReviews(@Param("userId") Long userId);

    /** 统计用户对特定互助任务的特定类型评价数量 */
    @Select("SELECT COUNT(*) FROM review WHERE reviewer_user_id = #{reviewerUserId} AND related_info_id = #{relatedInfoId} AND review_type = #{reviewType}")
    long countByReviewerAndInfoAndType(@Param("reviewerUserId") Long reviewerUserId,
            @Param("relatedInfoId") Long relatedInfoId,
            @Param("reviewType") String reviewType);
}
