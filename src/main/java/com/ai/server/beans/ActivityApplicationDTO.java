package com.ai.server.beans;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 活动申请DTO - 用于返回包含详细信息的申请记录
 * 包含申请人姓名、活动名称、审核人姓名等,避免返回ID
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActivityApplicationDTO {

    // 申请ID
    private Integer id;

    // 活动ID
    private Integer activityId;
    
    // 活动名称
    private String activityName;

    // 申请人ID (保留用于后端逻辑)
    private Integer userId;
    
    // 申请人姓名
    private String userName;
    
    // 申请人昵称
    private String userNickname;
    
    // 申请人手机号
    private String userPhone;

    // 申请状态 (pending, approved, rejected)
    private String status;

    // 申请时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime applyTime;

    // 审核时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime reviewTime;

    // 审核人ID (保留用于后端逻辑)
    private Integer reviewerId;
    
    // 审核人姓名
    private String reviewerName;
    
    // 审核人昵称
    private String reviewerNickname;

    // 创建时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createdAt;

    // 更新时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updatedAt;
}
