package com.example.campusbuddy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.campusbuddy.entity.HelpApplication;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface HelpApplicationMapper extends BaseMapper<HelpApplication> {
    @Select("SELECT u.nickname FROM help_application ha JOIN user u ON ha.applicant_id = u.user_id WHERE ha.application_id = #{applicationId}")
    String getApplicantNicknameByApplicationId(@Param("applicationId") Long applicationId);
}
