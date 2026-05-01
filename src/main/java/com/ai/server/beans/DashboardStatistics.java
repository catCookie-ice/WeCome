package com.ai.server.beans;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 管理员首页统计数据DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardStatistics implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 总用户数
     */
    private Integer userCount;
    
    /**
     * 用户增长百分比（较上月）
     */
    private Double userTrend;
    
    /**
     * 志愿活动总数
     */
    private Integer activityCount;
    
    /**
     * 活动增长百分比（较上月）
     */
    private Double activityTrend;
    
    /**
     * 待审核申请数
     */
    private Integer pendingApplications;
    
    /**
     * 申请数趋势百分比（较昨日）
     */
    private Double applicationTrend;
    
    /**
     * 待处理留言数
     */
    private Integer pendingMessages;
}
