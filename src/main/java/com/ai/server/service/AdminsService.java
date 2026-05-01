package com.ai.server.service;

import com.ai.server.beans.Admins;
import com.ai.server.beans.DashboardStatistics;

public interface AdminsService {

    Admins login(String username, String password);

    Admins getById(Integer replyId);

    /**
     * 获取管理员首页统计数据
     * @return 统计数据
     */
    DashboardStatistics getDashboardStatistics();

}
