package com.ai.server.mapper;

import com.ai.server.beans.ActivityApplication;
import com.ai.server.beans.ActivityApplicationDTO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ActivityApplicationMapper extends BaseMapper<ActivityApplication> {
    // MyBatis-Plus已提供基础CRUD方法,可在此添加自定义方法
    
    /**
     * 查询申请列表(带详细信息)
     * @param activityId 活动ID(可选)
     * @param userId 用户ID(可选)
     * @param status 状态(可选)
     * @return 申请列表(包含用户名、活动名等详细信息)
     */
    List<ActivityApplicationDTO> selectApplicationsWithDetails(
        @Param("activityId") Integer activityId,
        @Param("userId") Integer userId,
        @Param("status") String status
    );
    
    /**
     * 根据ID查询申请详情(带详细信息)
     * @param id 申请ID
     * @return 申请详情(包含用户名、活动名等详细信息)
     */
    ActivityApplicationDTO selectApplicationDetailById(@Param("id") Integer id);
    
    /**
     * 查询发布者的活动申请列表(带详细信息)
     * @param issuerId 发布者ID
     * @param status 状态(可选)
     * @return 申请列表
     */
    List<ActivityApplicationDTO> selectApplicationsByIssuer(
        @Param("issuerId") Integer issuerId,
        @Param("status") String status
    );
}

