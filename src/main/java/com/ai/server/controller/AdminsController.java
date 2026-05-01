package com.ai.server.controller;

import com.ai.server.beans.Admins;
import com.ai.server.beans.DashboardStatistics;
import com.ai.server.common.Result;
import com.ai.server.service.AdminsService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@Tag(name = "管理员接口", description = "管理员接口AI") // 类级注解
@RequestMapping("admins")
public class AdminsController {

    @Autowired
    AdminsService adminsService;


    /**
     * 管理员登录
     *
     * @return
     */
    @Operation(summary = "管理员登录", description = "管理员登录") // 方法级注解
    @PostMapping("login")
    public Result login(@RequestBody Admins admin) {
        log.info("管理员尝试登录: username={}", admin.getUsername());
        Admins a = adminsService.login(admin.getUsername(), admin.getPassword());
        if(a.getRole()!= admin.getRole())
            return Result.error("账号角色不匹配！");
        if (a != null) {
            //登录成功
            log.info("管理员登录成功: username={}", admin.getUsername());
            return Result.success(a);
        } else {
            log.warn("管理员登录失败: 账号或密码错误, username={}", admin.getUsername());
            return Result.error("账号或密码错误！");
        }
    }

    /**
     * 管理员首页统计数据
     *
     * @return 统计数据（用户、活动、申请、留言等核心指标）
     */
    @Operation(summary = "管理员首页统计数据", description = "返回管理员首页所需的统计数据，包括用户、活动、申请、留言等核心指标")
    @GetMapping("dashboard")
    public Result getDashboardStatistics() {
        log.info("获取管理员首页统计数据");
        DashboardStatistics statistics = adminsService.getDashboardStatistics();
        log.info("管理员首页统计数据返回成功");
        return Result.success(statistics);
    }

}
