package com.ai.server.controller;

import com.ai.server.beans.Members;
import com.ai.server.beans.VolunteerActivity;
import com.ai.server.common.Result;
import com.ai.server.exception.CustomException;
import com.ai.server.service.IMemberService;
import com.ai.server.service.VolunteerActivityService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@Slf4j
@Tag(name = "志愿活动接口", description = "志愿活动的新增、查询、更新、删除接口")
@RequestMapping("volunteer/activity") // 接口统一前缀
public class VolunteerActivityController {

    @Autowired
    private VolunteerActivityService activityService;
    @Autowired
    private IMemberService memberService;

    /**
     * 新增志愿活动
     */
    // 新增活动接口：自动接收 requiredPeople 参数（前端可传，也可省略用默认值）
    @Operation(summary = "新增志愿活动", description = "传入活动信息（含所需人数requiredPeople）")
    @PostMapping("add")
    public Result addActivity(@RequestBody VolunteerActivity activity) throws CustomException {
        log.info("新增志愿活动: activityName={}, initiatorId={}", activity.getActivityName(), activity.getInitiatorId());
        // 检查是否已存在相同的活动（排除已逻辑删除的记录）
        VolunteerActivity existingActivity = activityService.getOne(
                new QueryWrapper<VolunteerActivity>()
                        .eq("activity_name", activity.getActivityName())
                        .eq("created_at", activity.getCreatedAt())
                        .eq("del", 0)

        );
        Members publisher = memberService.getById(activity.getInitiatorId());
         if (publisher == null) {
             log.warn("发布者不存在: initiatorId={}", activity.getInitiatorId());
             throw new CustomException("发布者不存在");
         }
        if (existingActivity != null) {
            log.warn("活动已存在: activityName={}", activity.getActivityName());
            throw new CustomException("活动已存在");
        }

        // 设置默认值
        if (activity.getIsExpired() == null) {
            activity.setIsExpired(false);
        }
//        activity.setVolunteerIds(new ArrayList<>());
        if(activity.getRequiredPeople() <= 0)
        {
            activity.setRequiredPeople(10);
        }
        activity.setDel(0); // 设置删除状态为未删除

        log.info("准备保存志愿活动：{}", activity);
        boolean isSuccess = activityService.addActivity(activity);
        if (isSuccess) {
            log.info("新增志愿活动成功: activityName={}", activity.getActivityName());
        } else {
            log.error("新增志愿活动失败: activityName={}", activity.getActivityName());
        }
        return isSuccess ? Result.success("活动新增成功") : Result.error("活动新增失败");
    }

    /**
     * 根据活动名称模糊查询活动列表
     */
    @Operation(summary = "根据活动名称模糊查询分页", description = "根据活动名称模糊查询分页")
    @GetMapping("selectPage")
    public Result selectPage(@RequestParam("pageNum") Integer pageNum,
                             @RequestParam("pageSize") Integer pageSize,
                             @RequestParam("name") String name) {
        log.info("分页查询志愿活动: pageNum={}, pageSize={}, name={}", pageNum, pageSize, name);
        // 使用分页插件
        PageHelper.startPage(pageNum, pageSize);
        // 查询数据库，需要用到like查询
        List<VolunteerActivity> list = activityService.list(
                new QueryWrapper<VolunteerActivity>()
                        .like("activity_name", name)
                        .eq("del", 0)
                        .orderByDesc("recruit_start_time")
        );
        // 封装查询结果集
        PageInfo<VolunteerActivity> pageInfo = PageInfo.of(list);
        log.info("查询到 {} 条志愿活动记录", pageInfo.getTotal());
        return Result.success(pageInfo);
    }



    /**
     * 查询所有活动（支持按过期状态筛选）
     * @param isExpired false：未过期，true：过期，不传则查全部
     */
    @Operation(summary = "查询活动列表", description = "返回活动信息，包含所需人数")
    @GetMapping("list")
    public Result getAllActivities(@RequestParam(value = "isExpired", required = false) Boolean isExpired) {
        List<VolunteerActivity> activityList = activityService.getAllActivities(isExpired);
        return Result.success(activityList);
    }


    /**
     * 更新活动过期状态
     */
    @Operation(summary = "更新活动过期状态", description = "传入活动ID和状态（0/1），更新活动过期状态")
    @PutMapping("update/expired")
    public Result updateExpired(@RequestParam("id") Integer id, @RequestParam("isExpired") Boolean isExpired) {
        log.info("更新活动ID：{} 的过期状态为：{}", id, isExpired);
        // 校验状态值合法性
//        if (isExpired != 0 && isExpired != 1) {
//            return Result.error("状态值无效，仅支持0（未过期）或1（过期）");
//        }
        boolean isSuccess = activityService.updateActivityExpired(id, isExpired);
        if (isSuccess) {
            return Result.success("状态更新成功");
        }
        return Result.error("活动不存在或更新失败");
    }

    /**
     * 逻辑删除活动（更新 del=1）
     */
    @Operation(summary = "删除活动（逻辑删除）", description = "传入活动ID，标记活动为已删除（不实际删除数据）")
    @DeleteMapping("delete/{id}")
    public Result deleteActivity(@PathVariable("id") Integer id) {
        log.info("逻辑删除活动ID：{}", id);
        boolean isSuccess = activityService.deleteActivityById(id);
        if (isSuccess) {
            return Result.success("活动已逻辑删除");
        }
        return Result.error("活动不存在或已删除");
    }

    /**
     * 发布者查询自己的活动列表（分页）
     */
    @Operation(summary = "发布者查询自己的活动", description = "根据发布者ID查询其发布的所有活动，支持分页和按状态筛选")
    @GetMapping("publisher/list")
    public Result getPublisherActivities(
            @RequestParam("publisherId") Integer publisherId,
            @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
            @RequestParam(value = "isExpired", required = false) Integer isExpired) {
        
        log.info("发布者查询自己的活动: publisherId={}, pageNum={}, pageSize={}, isExpired={}", 
                 publisherId, pageNum, pageSize, isExpired);
        
        // 分页查询
        PageHelper.startPage(pageNum, pageSize);
        
        // 构建查询条件
        QueryWrapper<VolunteerActivity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("initiator_id", publisherId)  // 只查询该发布者的活动
                    .eq("del", 0)  // 未删除的活动
                    .orderByDesc("created_at");  // 按创建时间倒序
        
        // 如果指定了过期状态，添加过期条件
        if (isExpired != null) {
            queryWrapper.eq("is_expired", isExpired);
        }
        
        List<VolunteerActivity> list = activityService.list(queryWrapper);
        PageInfo<VolunteerActivity> pageInfo = PageInfo.of(list);
        
        log.info("发布者 {} 查询到 {} 条活动记录", publisherId, pageInfo.getTotal());
        return Result.success(pageInfo);
    }

    /**
     * 发布者修改自己的活动
     */
    @Operation(summary = "发布者修改活动", description = "发布者只能修改自己的活动，系统管理员可以修改所有活动")
    @PutMapping("publisher/update")
    public Result updateActivityByPublisher(
            @RequestBody VolunteerActivity activity,
            @RequestParam("publisherId") Integer publisherId,
            @RequestParam(value = "isAdmin", defaultValue = "false") Boolean isAdmin) {
        
        log.info("发布者修改活动: activityId={}, publisherId={}, isAdmin={}", 
                 activity.getId(), publisherId, isAdmin);
        
        // 1. 检查活动是否存在
        VolunteerActivity existingActivity = activityService.getById(activity.getId());
        if (existingActivity == null || existingActivity.getDel() == 1) {
            log.warn("活动不存在或已被删除: activityId={}", activity.getId());
            return Result.error("活动不存在或已被删除");
        }
        
        // 2. 权限校验：非管理员只能修改自己的活动
        if (!isAdmin && !existingActivity.getInitiatorId().equals(publisherId)) {
            log.warn("权限不足: publisherId={} 尝试修改 activityId={} (实际发布者: {})", 
                     publisherId, activity.getId(), existingActivity.getInitiatorId());
            return Result.error("您只能修改自己发布的活动");
        }
        
        // 3. 执行更新（只更新允许修改的字段）
        existingActivity.setActivityName(activity.getActivityName());
        existingActivity.setActivityDesc(activity.getActivityDesc());
        existingActivity.setActivityTime(activity.getActivityTime());
        existingActivity.setRecruitStartTime(activity.getRecruitStartTime());
        existingActivity.setRequiredPeople(activity.getRequiredPeople());
        
        // 管理员可以修改过期状态和删除标记
        if (isAdmin) {
            if (activity.getIsExpired() != null) {
                existingActivity.setIsExpired(activity.getIsExpired());
            }
        }
        
        boolean isSuccess = activityService.updateById(existingActivity);
        if (isSuccess) {
            log.info("活动修改成功: activityId={}", activity.getId());
            return Result.success("活动修改成功");
        } else {
            log.error("活动修改失败: activityId={}", activity.getId());
            return Result.error("活动修改失败");
        }
    }

    /**
     * 根据活动ID查询活动详情
     */
    @Operation(summary = "查询活动详情", description = "根据活动ID查询活动的详细信息")
    @GetMapping("detail/{id}")
    public Result getActivityDetail(@PathVariable("id") Integer id) {
        log.info("查询活动详情: activityId={}", id);
        
        VolunteerActivity activity = activityService.getById(id);
        if (activity == null || activity.getDel() == 1) {
            log.warn("活动不存在或已被删除: activityId={}", id);
            return Result.error("活动不存在或已被删除");
        }
        
        log.info("查询活动详情成功: activityId={}, activityName={}", id, activity.getActivityName());
        return Result.success(activity);
    }
}