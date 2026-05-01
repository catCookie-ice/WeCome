package com.ai.server.service.Impl;

import com.ai.server.beans.ActivityApplication;
import com.ai.server.mapper.ActivityApplicationMapper;
import com.ai.server.service.ActivityApplicationService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Slf4j
public class ActivityApplicationServiceImpl extends ServiceImpl<ActivityApplicationMapper, ActivityApplication>
        implements ActivityApplicationService {

    @Override
    public boolean applyForActivity(Integer activityId, Integer userId) {
        log.info("Service层: 用户申请活动: userId={}, activityId={}", userId, activityId);
        // 检查是否已申请过该活动（利用MyBatis-Plus的lambda查询）
        ActivityApplication existing = getOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ActivityApplication>()
                        .eq(ActivityApplication::getActivityId, activityId)
                        .eq(ActivityApplication::getUserId, userId)
                        .eq(ActivityApplication::getDel, 0)
        );

        if (existing != null) {
            log.warn("Service层: 用户已申请过该活动: userId={}, activityId={}", userId, activityId);
            return false; // 已申请过
        }

        // 创建新的申请记录
        ActivityApplication application = new ActivityApplication();
        application.setActivityId(activityId);
        application.setUserId(userId);
        application.setStatus("pending");
        application.setApplyTime(LocalDateTime.now());
        application.setCreatedAt(LocalDateTime.now());
        application.setUpdatedAt(LocalDateTime.now());

        boolean result = save(application);
        if (result) {
            log.info("Service层: 活动申请保存成功: applicationId={}", application.getId());
        } else {
            log.error("Service层: 活动申请保存失败");
        }
        return result;
    }

    @Override
    public boolean reviewApplication(Integer applicationId, Integer reviewerId, boolean approved) {
        log.info("Service层: 审核活动申请: reviewerId={}, applicationId={}, approved={}", reviewerId, applicationId, approved);
        // 利用MyBatis-Plus的lambda更新方式简化代码
        boolean result = update(
                new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<ActivityApplication>()
                        .eq(ActivityApplication::getId, applicationId)
                        .eq(ActivityApplication::getStatus, "pending")
                        .set(ActivityApplication::getStatus, approved ? "approved" : "rejected")
                        .set(ActivityApplication::getReviewerId, reviewerId)
                        .set(ActivityApplication::getReviewTime, LocalDateTime.now())
                        .set(ActivityApplication::getUpdatedAt, LocalDateTime.now())
        );
        if (result) {
            log.info("Service层: 活动申请审核完成: applicationId={}, status={}", applicationId, approved ? "approved" : "rejected");
        } else {
            log.error("Service层: 活动申请审核失败: applicationId={}", applicationId);
        }
        return result;
    }
}
