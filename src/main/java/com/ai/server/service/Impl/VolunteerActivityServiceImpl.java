package com.ai.server.service.Impl;

import com.ai.server.beans.VolunteerActivity;
import com.ai.server.mapper.VolunteerActivityMapper;
import com.ai.server.service.VolunteerActivityService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;

// 继承 ServiceImpl<Mapper类型, 实体类类型>，自动注入 Mapper 并封装基础方法
@Service
@Slf4j
public class VolunteerActivityServiceImpl extends ServiceImpl<VolunteerActivityMapper, VolunteerActivity>
        implements VolunteerActivityService {

    // 无需手动注入 Mapper，父类 ServiceImpl 已自动注入并提供 getBaseMapper() 方法


    // 1. 新增志愿活动（复用父类 save 方法，底层调用 Mapper.insert）
    @Override
    public boolean addActivity(VolunteerActivity activity) {
        log.info("Service层: 保存志愿活动到数据库: activityName={}", activity.getActivityName());
        // 业务逻辑：设置默认值
        if (activity.getIsExpired() == null) {
            activity.setIsExpired(false);
        }
        if (activity.getRequiredPeople() == null) {
            activity.setRequiredPeople(10);
        }
        // 调用父类 save 方法（替代 activityMapper.insert(activity)）
        boolean result = save(activity);
        if (result) {
            log.info("Service层: 志愿活动保存成功: activityId={}", activity.getId());
        } else {
            log.error("Service层: 志愿活动保存失败");
        }
        return result;
    }


    // 2. 根据ID查询单个活动（复用父类 getById 方法，底层调用 Mapper.selectById）
    @Override
    public VolunteerActivity getActivityById(Integer id) {
        // 调用父类 getById 方法（替代 activityMapper.selectById(id)）
        return getById(id);
    }

    // 根据活动名称模糊查询活动列表（复用父类 list 方法）
    @Override
    public List<VolunteerActivity> getActivityByName(String name) {
        QueryWrapper<VolunteerActivity> queryWrapper = new QueryWrapper<>();
        // 添加对 del 字段的判断，只查询未删除的记录
        queryWrapper.eq("del", 0);
        // 模糊查询活动名称
        queryWrapper.like("activity_name", name);
        queryWrapper.orderByDesc("recruit_start_time");
        // 调用父类 list 方法
        return list(queryWrapper);
    }


    // 3. 查询所有活动（支持按过期状态筛选）
    @Override
    public List<VolunteerActivity> getAllActivities(Boolean isExpired) {
        QueryWrapper<VolunteerActivity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("del", 0);
        if (isExpired != null) {
            queryWrapper.eq("is_expired", isExpired);
        }
        queryWrapper.orderByDesc("recruit_start_time");
        // 调用父类 list 方法（替代 activityMapper.selectList(queryWrapper)）
        return list(queryWrapper);
    }


    // 4. 更新活动过期状态（复用父类 updateById 方法，底层调用 Mapper.updateById）
    @Override
    public boolean updateActivityExpired(Integer id, Boolean isExpired) {
        // 先查询活动是否存在（调用父类 getById）
        VolunteerActivity activity = getById(id);
        if (activity == null) {
            return false;
        }
        // 设置新状态并更新
        activity.setIsExpired(isExpired);
        // 调用父类 updateById 方法（替代 activityMapper.updateById(activity)）
        return updateById(activity);
    }


    // 5. 逻辑删除活动（复用父类 removeById 方法，底层自动执行逻辑删除）
    @Override
    public boolean deleteActivityById(Integer id) {
        // 先查询活动是否存在且未删除（调用父类 getById）
        VolunteerActivity activity = getById(id);
        if (activity == null || activity.getDel() == 1) {
            return false;
        }
        // 调用父类 removeById 方法（替代 activityMapper.deleteById(id)，自动执行逻辑删除）
        return removeById(id);
    }
}