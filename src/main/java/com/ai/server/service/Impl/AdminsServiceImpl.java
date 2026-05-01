package com.ai.server.service.Impl;

import com.ai.server.beans.Admins;
import com.ai.server.beans.DashboardStatistics;
import com.ai.server.beans.ActivityApplication;
import com.ai.server.beans.Leavemsg;
import com.ai.server.beans.Members;
import com.ai.server.beans.VolunteerActivity;
import com.ai.server.mapper.AdminsMapper;
import com.ai.server.mapper.ActivityApplicationMapper;
import com.ai.server.mapper.LeavemsgMapper;
import com.ai.server.mapper.MemberMapper;
import com.ai.server.mapper.VolunteerActivityMapper;
import com.ai.server.service.AdminsService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class AdminsServiceImpl implements AdminsService {

    @Autowired
    AdminsMapper adminsMapper;

    @Autowired
    private MemberMapper memberMapper;

    @Autowired
    private VolunteerActivityMapper activityMapper;

    @Autowired
    private ActivityApplicationMapper applicationMapper;

    @Autowired
    private LeavemsgMapper leavemsgMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    // Redis键前缀
    private static final String DASHBOARD_STATS_PREFIX = "dashboard:stats:";
    // 缓存过期时间（分钟）
    private static final long CACHE_EXPIRE_TIME = 30;

    @Override
    public Admins login(String username, String password) {
        log.info("管理员尝试登录: username={}", username);
        return adminsMapper.selectOne
                (new QueryWrapper<Admins>().eq("username", username).
                        eq("password",password).
                        eq("del", "0"));
    }
    
    @Override
    public Admins getById(Integer id) {
        return adminsMapper.selectById(id);
    }

    @Override
    public DashboardStatistics getDashboardStatistics() {
        log.info("开始获取管理员首页统计数据");
        
        // 先从Redis缓存中获取
        String cacheKey = DASHBOARD_STATS_PREFIX + "all";
        try {
            Object cachedData = redisTemplate.opsForValue().get(cacheKey);
            if (cachedData != null) {
                log.info("从Redis缓存中获取到管理员首页统计数据");
                return (DashboardStatistics) cachedData;
            }
        } catch (Exception e) {
            log.warn("从Redis获取缓存数据失败，将从数据库重新查询", e);
        }
        
        // 缓存未命中，从数据库查询
        log.info("Redis缓存未命中，从数据库查询统计数据");
        
        // 1. 计算总用户数
        Long userCount = memberMapper.selectCount(
            new QueryWrapper<Members>().eq("del", "0")
        );
        
        // 2. 计算用户增长趋势（本月新增 vs 上月新增）
        Double userTrend = calculateUserTrend();
        
        // 3. 计算活动总数
        Long activityCount = activityMapper.selectCount(
            new QueryWrapper<VolunteerActivity>().eq("del", 0)
        );
        
        // 4. 计算活动增长趋势（本月新增 vs 上月新增）
        Double activityTrend = calculateActivityTrend();
        
        // 5. 计算待审核申请数
        Long pendingApplications = applicationMapper.selectCount(
            new QueryWrapper<ActivityApplication>().eq("status", "PENDING")
        );
        
        // 6. 计算申请数趋势（今日新增 vs 昨日新增）
        Double applicationTrend = calculateApplicationTrend();
        
        // 7. 计算待处理留言数
        Long pendingMessages = leavemsgMapper.selectCount(
            new QueryWrapper<Leavemsg>()
                .eq("del", "0")
                .and(wrapper -> wrapper.isNull("reply").or().eq("reply", ""))
        );
        
        DashboardStatistics statistics = DashboardStatistics.builder()
            .userCount(userCount.intValue())
            .userTrend(userTrend)
            .activityCount(activityCount.intValue())
            .activityTrend(activityTrend)
            .pendingApplications(pendingApplications.intValue())
            .applicationTrend(applicationTrend)
            .pendingMessages(pendingMessages.intValue())
            .build();
        
        // 将查询结果存入Redis缓存
        try {
            redisTemplate.opsForValue().set(cacheKey, statistics, CACHE_EXPIRE_TIME, TimeUnit.MINUTES);
            log.info("管理员首页统计数据已存入Redis缓存，有效期{}分钟", CACHE_EXPIRE_TIME);
        } catch (Exception e) {
            log.error("将统计数据存入Redis缓存失败", e);
        }
        
        log.info("管理员首页统计数据获取成功: {}", statistics);
        return statistics;
    }
    
    /**
     * 计算用户增长趋势（本月 vs 上月）
     */
    private Double calculateUserTrend() {
        try {
            LocalDateTime now = LocalDateTime.now();
            String currentMonth = now.format(DateTimeFormatter.ofPattern("yyyy-MM"));
            String lastMonth = now.minusMonths(1).format(DateTimeFormatter.ofPattern("yyyy-MM"));
            
            // 本月新增用户数 (createtime 是字符串类型)
            Long currentMonthCount = memberMapper.selectCount(
                new QueryWrapper<Members>()
                    .eq("del", "0")
                    .likeRight("createtime", currentMonth)
            );
            
            // 上月新增用户数
            Long lastMonthCount = memberMapper.selectCount(
                new QueryWrapper<Members>()
                    .eq("del", "0")
                    .likeRight("createtime", lastMonth)
            );
            
            if (lastMonthCount == 0) {
                return currentMonthCount > 0 ? 100.0 : 0.0;
            }
            
            return ((currentMonthCount - lastMonthCount) * 100.0) / lastMonthCount;
        } catch (Exception e) {
            log.error("计算用户增长趋势失败", e);
            return 0.0;
        }
    }
    
    /**
     * 计算活动增长趋势（本月 vs 上月）
     */
    private Double calculateActivityTrend() {
        try {
            LocalDateTime now = LocalDateTime.now();
            String currentMonth = now.format(DateTimeFormatter.ofPattern("yyyy-MM"));
            String lastMonth = now.minusMonths(1).format(DateTimeFormatter.ofPattern("yyyy-MM"));
            
            // 本月新增活动数 (created_at 是 LocalDateTime)
            Long currentMonthCount = activityMapper.selectCount(
                new QueryWrapper<VolunteerActivity>()
                    .eq("del", 0)
                    .apply("DATE_FORMAT(created_at, '%Y-%m') = {0}", currentMonth)
            );
            
            // 上月新增活动数
            Long lastMonthCount = activityMapper.selectCount(
                new QueryWrapper<VolunteerActivity>()
                    .eq("del", 0)
                    .apply("DATE_FORMAT(created_at, '%Y-%m') = {0}", lastMonth)
            );
            
            if (lastMonthCount == 0) {
                return currentMonthCount > 0 ? 100.0 : 0.0;
            }
            
            return ((currentMonthCount - lastMonthCount) * 100.0) / lastMonthCount;
        } catch (Exception e) {
            log.error("计算活动增长趋势失败", e);
            return 0.0;
        }
    }
    
    /**
     * 计算申请数趋势（今日 vs 昨日）
     */
    private Double calculateApplicationTrend() {
        try {
            LocalDateTime now = LocalDateTime.now();
            String today = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            String yesterday = now.minusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            
            // 今日申请数 (created_at 是 LocalDateTime)
            Long todayCount = applicationMapper.selectCount(
                new QueryWrapper<ActivityApplication>()
                    .apply("DATE_FORMAT(created_at, '%Y-%m-%d') = {0}", today)
            );
            
            // 昨日申请数
            Long yesterdayCount = applicationMapper.selectCount(
                new QueryWrapper<ActivityApplication>()
                    .apply("DATE_FORMAT(created_at, '%Y-%m-%d') = {0}", yesterday)
            );
            
            if (yesterdayCount == 0) {
                return todayCount > 0 ? 100.0 : 0.0;
            }
            
            return ((todayCount - yesterdayCount) * 100.0) / yesterdayCount;
        } catch (Exception e) {
            log.error("计算申请数趋势失败", e);
            return 0.0;
        }
    }
}
