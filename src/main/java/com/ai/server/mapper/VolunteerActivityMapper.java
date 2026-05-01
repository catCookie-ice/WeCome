package com.ai.server.mapper;

import com.ai.server.beans.VolunteerActivity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * 志愿活动Mapper接口，继承BaseMapper获取基础CRUD方法
 */
public interface VolunteerActivityMapper extends BaseMapper<VolunteerActivity> {

    // 无需额外定义基础方法，BaseMapper已包含：selectById、insert、updateById、deleteById等
}