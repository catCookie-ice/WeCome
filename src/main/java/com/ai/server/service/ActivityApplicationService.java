package com.ai.server.service;

import com.ai.server.beans.ActivityApplication;
import com.baomidou.mybatisplus.extension.service.IService;

public interface ActivityApplicationService extends IService<ActivityApplication> {
    // 可在此处定义自定义业务方法
    boolean applyForActivity(Integer activityId, Integer userId);

    boolean reviewApplication(Integer applicationId, Integer reviewerId, boolean approved);
}
