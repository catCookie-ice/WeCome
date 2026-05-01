package com.ai.server.beans;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("volunteer_activity") // 优化1：补上表名
public class VolunteerActivity {

    // 优化2：补上主键注解
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @TableField("activity_name")
    private String activityName;

    @TableField("activity_desc")
    private String activityDesc;

    @TableField("activity_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime activityTime;

    @TableField("recruit_start_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime recruitStartTime;

    @TableField("initiator_id")
    private Integer initiatorId;

    // 🚨 优化3：【直接删除 volunteerIds 字段】！
    // 报名志愿者的关系，必须通过 activity_application 表去关联查询，绝不能在这里存列表！

    // 优化4：增加完善的状态字段 (0-草稿, 1-招募中, 2-进行中, 3-已结束)
    @TableField("status")
    private Integer status = 0;

    @TableField("required_people")
    private Integer requiredPeople;

    // 优化5：is开头改为 Boolean 类型，并在数据库映射为 TINYINT(1)
    @TableField("is_expired")
    private Boolean isExpired = false;

    @TableField("is_full")
    private Boolean isFull = false;

    @TableField("created_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createdAt;

    @TableField("del")
    @TableLogic
    private Integer del = 0;

    // 扩展字段：如果列表页需要展示“已报名人数”，不要用List算，而是用SQL查出来塞这里
    @TableField(exist = false)
    private Integer appliedCount;
}
