package com.example.campusbuddy.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.math.BigDecimal;

/**
 * 创建/更新互助任务的数据传输对象
 */
@Data
public class HelpInfoDTO {

    @NotBlank(message = "互助类型不能为空")
    @Pattern(regexp = "COURSE_TUTORING|SKILL_LEARNING|ITEM_LEND|ITEM_EXCHANGE|TEAM_UP", message = "互助类型必须是：课程辅导、技能学习、物品借用、物品交换或组队合作")
    private String type;

    @NotBlank(message = "标题不能为空")
    @Size(min = 2, max = 50, message = "标题长度必须在2-50个字符之间")
    private String title;

    @NotBlank(message = "描述不能为空")
    @Size(min = 10, max = 500, message = "描述长度必须在10-500个字符之间")
    private String description;

    @NotBlank(message = "预期时间不能为空")
    private String expectedTime;

    @NotBlank(message = "预期地点不能为空")
    private String expectedLocation;

    @NotBlank(message = "联系方式不能为空")
    private String contactMethod;

    @DecimalMin(value = "0.0", inclusive = true, message = "报酬金额不能为负数")
    private BigDecimal rewardAmount;

    private String imageUrls; // JSON字符串，可为空
}
