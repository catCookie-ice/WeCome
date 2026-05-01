package com.ai.server.service;

import com.ai.server.beans.VolunteerActivity;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface VolunteerActivityService  extends IService<VolunteerActivity> {
    // 现有方法保持不变{

    // 1. 新增志愿活动
    boolean addActivity(VolunteerActivity activity);

    // 2. 根据ID查询单个活动
    VolunteerActivity getActivityById(Integer id);

    // 2. 根据活动名称模糊查询活动列表（复用父类 list 方法）
    List<VolunteerActivity> getActivityByName(String name);

    // 3. 查询所有活动（可按过期状态筛选，默认查全部）
    List<VolunteerActivity> getAllActivities(Boolean isExpired);

    // 4. 更新活动过期状态（0→1或1→0）
    boolean updateActivityExpired(Integer id, Boolean isExpired);

    // 5. 根据ID删除活动（逻辑删除可扩展，此处为物理删除）
    boolean deleteActivityById(Integer id);

}
