package com.ai.server.controller;

import com.ai.server.beans.ActivityApplication;
import com.ai.server.beans.ActivityApplicationDTO;
import com.ai.server.beans.Members;
import com.ai.server.beans.VolunteerActivity;
import com.ai.server.common.Result;
import com.ai.server.enums.MemberRoleEnum; // 引入枚举
import com.ai.server.mapper.ActivityApplicationMapper;
import com.ai.server.service.ActivityApplicationService;
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

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/application")
@Slf4j
@Tag(name = "活动申请接口", description = "活动申请相关操作接口")
public class ActivityApplicationController {

    @Autowired
    private ActivityApplicationService applicationService;

    @Autowired
    private ActivityApplicationMapper applicationMapper;
    // 在类字段中添加如下内容（建议放在 applicationService 下方）：
    @Autowired
    private VolunteerActivityService activityService;
    @Autowired
    private IMemberService memberService;
    /**
     * 用户申请活动
     */
//    @Operation(summary = "申请活动", description = "用户申请参加指定活动")
//    @PostMapping("/apply")
//    public Result applyForActivity(@RequestParam("activityId") Integer activityId, @RequestParam("userId") Integer userId) {
//        log.info("用户申请活动: userId={}, activityId={}", userId, activityId);
//        boolean success = applicationService.applyForActivity(activityId, userId);
//        if (success) {
//            log.info("申请活动成功: userId={}, activityId={}", userId, activityId);
//            return Result.success("申请成功");
//        }
//        log.warn("申请活动失败: userId={}, activityId={} - 可能已申请过该活动", userId, activityId);
//        return Result.error("申请失败，可能已申请过该活动");
//    }

    /**
     * 管理员审核申请
     */
    @Operation(summary = "审核申请", description = "管理员审核用户的活动申请")
    @PutMapping("/review")
    public Result reviewApplication(
            @RequestParam("applicationId") Integer applicationId,  // 显式指定参数名
            @RequestParam("reviewerId") Integer reviewerId,        // 显式指定参数名
            @RequestParam("approved") Boolean approved) {          // 显式指定参数名
        log.info("管理员审核活动申请: reviewerId={}, applicationId={}, approved={}", reviewerId, applicationId, approved);
        boolean success = applicationService.reviewApplication(applicationId, reviewerId, approved);
        if (success) {
            log.info("审核活动申请成功: applicationId={}, approved={}", applicationId, approved);
            return Result.success("审核成功");
        }
        log.error("审核活动申请失败: applicationId={}", applicationId);
        return Result.error("审核失败");
    }

    /**
     * 分页查询申请记录
     */
    @Operation(summary = "分页查询申请", description = "分页查询活动申请记录")
    @GetMapping("/page")
    public Result selectPage(
            @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
            @RequestParam(value = "activityId", required = false) Integer activityId,
            @RequestParam(value = "userId", required = false) Integer userId,
            @RequestParam(value = "status", required = false) String status) {

        log.info("分页查询活动申请: pageNum={}, pageSize={}, activityId={}, userId={}, status={}", 
                 pageNum, pageSize, activityId, userId, status);
        PageHelper.startPage(pageNum, pageSize);

        // 使用自定义Mapper方法查询,返回包含详细信息的DTO
        List<ActivityApplicationDTO> list = applicationMapper.selectApplicationsWithDetails(activityId, userId, status);
        PageInfo<ActivityApplicationDTO> pageInfo = PageInfo.of(list);
        log.info("查询到 {} 条活动申请记录", pageInfo.getTotal());
        return Result.success(pageInfo);
    }


    /**
     * 根据ID查询申请详情
     */
    @Operation(summary = "查询申请详情", description = "根据ID查询活动申请详情")
    @GetMapping("/{id}")
    public Result getApplicationById(@PathVariable Integer id) {
        ActivityApplicationDTO application = applicationMapper.selectApplicationDetailById(id);
        if (application != null) {
            return Result.success(application);
        }
        return Result.error("申请记录不存在");
    }

    /**
     * 发布者查询自己活动的申请列表(分页)
     * 支持按状态过滤: pending(待处理)、approved(已通过)、rejected(已拒绝)
     */
    @Operation(summary = "发布者查询申请", description = "发布者查询自己发布的活动的申请列表,支持按状态过滤")
    @GetMapping("/issuer/applications")
    public Result getIssuerApplications(
            @RequestParam(value = "issuerId") Integer issuerId,
            @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
            @RequestParam(value = "status", required = false) String status) {
        
        log.info("发布者查询申请列表: issuerId={}, pageNum={}, pageSize={}, status={}", 
                 issuerId, pageNum, pageSize, status);
        PageHelper.startPage(pageNum, pageSize);
        
        // 使用自定义Mapper方法查询发布者的申请列表
        List<ActivityApplicationDTO> list = applicationMapper.selectApplicationsByIssuer(issuerId, status);
        PageInfo<ActivityApplicationDTO> pageInfo = PageInfo.of(list);
        log.info("发布者 {} 查询到 {} 条申请记录", issuerId, pageInfo.getTotal());
        return Result.success(pageInfo);
    }

    /**
     * 发布者处理申请(同意或拒绝)
     */
    @Operation(summary = "发布者处理申请", description = "发布者同意或拒绝用户的活动申请")
    @PutMapping("/issuer/process")
    public Result processApplicationByIssuer(@RequestParam("applicationId") Integer applicationId,
                                             @RequestParam("issuerId") Integer issuerId,
                                             @RequestParam("approved") Boolean approved) {
        // 验证申请记录是否存在
        ActivityApplication application = applicationService.getById(applicationId);
        if (application == null || application.getDel() == 1) {
            return Result.error("申请记录不存在");
        }

        // 检查该活动是否属于此发布者
        VolunteerActivity activity = activityService.getOne(
                new QueryWrapper<VolunteerActivity>()
                        .eq("id", application.getActivityId())
                        .eq("initiator_id", issuerId)
                        .eq("del", 0)
        );

        if (activity == null) {
            return Result.error("无权限处理此申请");
        }

        // 处理申请
        boolean success = applicationService.reviewApplication(applicationId, issuerId, approved);
        if (success) {
            // 处理成功后逻辑删除申请记录
            if (Boolean.TRUE.equals(approved)) {
                Long approvedCount = applicationService.count(
                        new QueryWrapper<ActivityApplication>()
                                .eq("activity_id", application.getActivityId())
                                .eq("status", "approved")
                                .eq("del", 0)
                );

                // 判断是否满员
                if (approvedCount != null && approvedCount >= activity.getRequiredPeople()) {
                    activity.setIsFull(true); // 修复：使用 Boolean 的 true
                    activityService.updateById(activity);
                }
            }
            return Result.success("处理成功");

//            ActivityApplication updateApplication = new ActivityApplication();
//            updateApplication.setId(applicationId);
//            updateApplication.setDel(1);
//            updateApplication.setUpdatedAt(LocalDateTime.now());
//            applicationService.updateById(updateApplication);
//            //如果满员，则将活动状态改为满员
//            if (activity.getVolunteerIds().size() >= activity.getRequiredPeople()) {
//                activity.setIs_full(1);
//                activityService.updateById(activity);
//            }
//            return Result.success("处理成功");
        }
        return Result.error("处理失败");
    }

    /**
     * 会员申请参加活动
     */
    @Operation(summary = "会员申请活动", description = "会员申请参加指定活动")
    @PostMapping("/member/apply")
    public Result memberApply(@RequestParam(value = "activityId") Integer activityId,
                          @RequestParam(value = "memberId") Integer memberId) {
    // 检查活动是否存在且未过期
    VolunteerActivity activity = activityService.getOne(
            new QueryWrapper<VolunteerActivity>()
                    .eq("id", activityId)
                    .eq("is_expired", false)
                    .eq("del", 0)
    );

    if (activity == null) {
        return Result.error("活动不存在或已过期");
    }
    //如果memberId对应的role是Issuer，则不能申请的活动
       //通过memberId获取member对象
       Members member = memberService.getById(memberId);
    if (MemberRoleEnum. PUBLISHER.equals(member.getRole())) {
        return Result.error("发布者不能申请活动");
    }

    //如果活动已经满员//安全地判断 Boolean 类型是否满员
       if (Boolean.TRUE.equals(activity.getIsFull())) {
           return Result.error("活动已满员");
       }
    // 获取活动发起人ID
    Integer initiatorId = activity.getInitiatorId();

    // 检查是否已申请过该活动
    ActivityApplication existing = applicationService.getOne(
            new QueryWrapper<ActivityApplication>()
                    .eq("activity_id", activityId)
                    .eq("volunteer_id", memberId)
                    .eq("del", 0)
    );

    if (existing != null) {
        return Result.error("已申请过该活动");
    }

    // 创建申请记录
    ActivityApplication application = new ActivityApplication();
    application.setActivityId(activityId);
    application.setUserId(memberId);
    application.setReviewerId(initiatorId); // 填入发起人ID
    application.setStatus("pending");
    application.setApplyTime(LocalDateTime.now());
    application.setCreatedAt(LocalDateTime.now());
    application.setUpdatedAt(LocalDateTime.now());

    boolean success = applicationService.save(application);
    if (success) {
        return Result.success("申请成功，请等待发布者审核");
    }
    return Result.error("申请失败");
}


    /**
     * 检查发布者待处理申请数量
     */
    @Operation(summary = "检查待处理申请", description = "发布者登录时检查待处理的申请数量")
    @GetMapping("/issuer/pending/count")
    public Result getPendingApplicationsCount(@RequestParam(value = "issuerId") Integer issuerId) {
        // 查询该发布者待处理的申请数量
        Long count = applicationService.count(
                new QueryWrapper<ActivityApplication>()
                        .eq("del", 0)
                        .eq("status", "pending")
                        .apply("EXISTS (SELECT 1 FROM volunteer_activity va WHERE va.id = activity_application.activity_id AND va.initiator_id = {0} AND va.del = 0)", issuerId)
        );

        return Result.success(count);
    }




}
