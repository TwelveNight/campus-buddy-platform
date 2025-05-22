package com.example.campusbuddy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.campusbuddy.entity.Review;
import com.example.campusbuddy.vo.ReviewVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ReviewMapper extends BaseMapper<Review> {
    List<ReviewVO> selectReviewVOsByHelpId(@Param("helpId") Long helpId);

    List<ReviewVO> selectReviewVOsByUserId(@Param("userId") Long userId);

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
}
