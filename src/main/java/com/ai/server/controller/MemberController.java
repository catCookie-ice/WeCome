package com.ai.server.controller;


import cn.hutool.core.date.DateTime;
import com.ai.server.beans.Members;
import com.ai.server.beans.VolunteerActivity;
import com.ai.server.common.Result;
import com.ai.server.enums.MemberRoleEnum;
import com.ai.server.exception.CustomException;
import com.ai.server.service.IMemberService;
import com.ai.server.service.VolunteerActivityService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import org.springframework.stereotype.Controller;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 * @author bruce
 */
@RestController
@RequestMapping("/member")
@Slf4j
@Tag(name = "会员接口", description = "会员接口AI") // 类级注解
public class MemberController {

    @Resource
    IMemberService memberService;
    @Autowired
    private VolunteerActivityService volunteeractivityservice;

    private static final String ROOT_PATH = System.getProperty("user.dir") + "/files";
    /**
     * 分页查询
     * @return
     */
    @Operation(summary = "根据昵称模糊查询分页", description = "根据昵称模糊查询分页") // 方法级注解
    @GetMapping("selectPage")
    public Result selectPage(@RequestParam("pageNum") Integer pageNum,
                             @RequestParam("pageSize") Integer pageSize,
                             @RequestParam("nickname") String nickname){
        log.info("分页查询会员: pageNum={}, pageSize={}, nickname={}", pageNum, pageSize, nickname);
        //使用分页插件
        PageHelper.startPage(pageNum,pageSize);
        //查询数据库，需要用到like查询
        List<Members> list = memberService.list(new QueryWrapper<Members>().like("nickname", nickname).eq("del", "0").orderByDesc("id"));
        //封装一下查询的结果集
        PageInfo<Members> pageInfo = PageInfo.of(list);
        log.info("查询到 {} 条会员记录", pageInfo.getTotal());
        return Result.success(pageInfo);
    }

    /**
     * 新增接口
     * @return
     */
    @Operation(summary = "新增会员信息", description = "新增会员信息") // 方法级注解
    @PostMapping("add")
    public Result add(@RequestBody Members member) throws CustomException {
        log.info("新增会员: {}", member);
        Members one = memberService.getOne(new QueryWrapper<Members>().eq("uname", member.getUname()).eq("del", "0"));
        if(one==null){
            member.setDel(0);//删除状态
            member.setCreatetime(LocalDateTime.now()); //设置系统时间
            boolean b = memberService.save(member);
            if(b){
                log.info("新增会员成功: {}", member.getUname());
                return Result.success();
            }else{
                log.error("新增会员失败: {}", member.getUname());
                return Result.error();
            }
        }else{
            log.warn("会员账号已存在: {}", member.getUname());
            throw new CustomException("会员账号已存在");
        }
    }

    /**
     *  更新
     */
    @Operation(summary = "更新会员信息", description = "更新会员信息") // 方法级注解
    @PutMapping("update")
    public Result update(@RequestBody Members member){
        log.info("更新会员信息: memberId={}, uname={}", member.getId(), member.getUname());
//        delete_oldphoto(member);//此方法会同时物理删除老图片文件

        boolean b = memberService.updateById(member);
        if(b){
            log.info("更新会员信息成功: memberId={}", member.getId());
            return Result.success();
        }else{
            log.error("更新会员信息失败: memberId={}", member.getId());
            return Result.error();
        }

    }

    private boolean delete_oldphoto(Members member){
        //判断账户数据库中的photots与新的photos是否一致
        // 优化后的代码
        Members oldMember = memberService.getById(member.getId());
        if (oldMember != null && oldMember.getPhoto() != null) {

            // 只有当新旧照片不同时才删除旧照片
            if (!oldMember.getPhoto().equals(member.getPhoto())) {

                //取得老照片最后一级目录 后的路径
                String oldPhotos = oldMember.getPhoto().substring(oldMember.getPhoto().lastIndexOf("/") + 1);
                File oldFile = new File(ROOT_PATH + "/" + oldPhotos );
                if (oldFile.exists()) {
                    boolean deleted = oldFile.delete();
                    log.info("删除旧文件结果: " + deleted + ", 文件: " + oldFile.getAbsolutePath());
                    return true ;
                }
            }
            return false;
        }
        return false;
    }


    /**
     * 删除
     */
    @Operation(summary = "根据ID删除会员信息", description = "根据ID删除会员信息") // 方法级注解
    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable("id") Integer id){
        log.info("删除会员: id={}", id);
        Members member = new Members();
        member.setId(id);
        member.setDel(1);//删除
        boolean b = memberService.updateById(member);
        if(b){
            log.info("删除会员成功: id={}", id);
            return Result.success();
        }else{
            log.error("删除会员失败: id={}", id);
            return Result.error();
        }
    }


    /**
     * 会员登录
     * @return
     */
    @PostMapping("login")
    @Operation(summary = "会员登录", description = "会员登录") // 方法级注解
    public Result login(@RequestBody Members member){
        log.info("会员尝试登录: uname={}", member.getUname());
        Members a = memberService.getOne(new QueryWrapper<Members>().eq("uname", member.getUname()).eq("upass",member.getUpass()));
        if(a.getRole()!= member.getRole())
            return Result.error("账号角色不匹配！");
        if(a!=null){
            log.info("会员登录成功: uname={}", member.getUname());
            return Result.success(a);
        }else{
            log.warn("会员登录失败: 账号或密码错误, uname={}", member.getUname());
            return Result.error("账号或密码错误！");
        }
    }


    /**
     * 新增接口
     * @return
     */
    @Operation(summary = "注册会员信息", description = "注册会员信息") // 方法级注解
    @PostMapping("register")
    public Result register(@RequestBody Members member) throws CustomException {
        log.info("注册的信息是:"+member);
        Members one = memberService.getOne(new QueryWrapper<Members>().eq("uname", member.getUname()).eq("del", "0"));
        if(one==null){
            member.setDel(0);//删除状态
            member.setCreatetime(LocalDateTime.now()); //设置系统时间
            boolean b = memberService.save(member);
            if(b){
                return Result.success();
            }else{
                return Result.error();
            }
        }else{
            throw new CustomException("会员账号已存在");
        }
    }

    //Issuer登录时
    @Operation(summary = "Issuer登录后", description = "Issuer登录后检查并更新活动是否有过期")
    @PostMapping("/issuer/updateDataBase")
    public Result issuerLoad(@RequestBody Members issuer){
        log.info("登录的信息是:"+issuer);
        Members a = memberService.getOne(new QueryWrapper<Members>().eq("uname", issuer.getUname()).eq("upass",issuer.getUpass()));
        
        // 校验登录信息
        if (a == null) {
            log.warn("Issuer登录失败: 用户名或密码错误, uname={}", issuer.getUname());
            return Result.error("用户名或密码错误！");
        }
        
        //获取Issuer所发起的所有志愿者活动
        List<VolunteerActivity> activity_list = volunteeractivityservice.list(new QueryWrapper<VolunteerActivity>().eq("initiator_id ", a.getId())
                .eq("is_expired",0));


        if(activity_list!=null){
            //如果现在的时间超过了活动的结束时间至少一天，则将活动状态改为已结束
            for(VolunteerActivity activity:activity_list){
                if(activity.getActivityTime().isBefore(LocalDateTime.now())){
                    activity.setIsExpired(true);
                    volunteeractivityservice.updateById(activity);
                }
            }
        }
        log.info("用户{}数据库已完成更新", a.getUname());
        return Result.success();
    }

    @Operation(summary = "TENANT登录后", description = "TENANT登录后")
    @PostMapping("/tenant/load")
    public Result tenantLoad(@RequestBody Members tenant){
        log.info("登录的信息是:"+tenant);
        Members a = memberService.getOne(new QueryWrapper<Members>().eq("uname", tenant.getUname()).eq("upass",tenant.getUpass()));
        
        // 校验登录信息
        if (a == null) {
            log.warn("TENANT登录失败: 用户名或密码错误, uname={}", tenant.getUname());
            return Result.error("用户名或密码错误！");
        }
        
        log.info("TENANT登录成功: uname={}, id={}", a.getUname(), a.getId());
        
        //获取TENANT所参加的所有志愿者活动(a的id在volunteer_activity的volunteer_ids中)
        List<VolunteerActivity> activity_list = volunteeractivityservice.list(// 修复Bug4: 避免 SQL 注入，使用 {0} 进行参数化绑定
                new QueryWrapper<VolunteerActivity>().apply(("JSON_CONTAINS(volunteer_ids, {0})" + a.getId())));

        if(activity_list!=null){
            //如果现在的时间超过了活动的结束时间至少一天，则将活动状态改为已结束
            for(VolunteerActivity activity:activity_list){
                if(activity.getActivityTime().isBefore(LocalDateTime.now())&&activity.getIsFull()){
                    activity.setIsExpired(true);
                    volunteeractivityservice.updateById(activity);
                }
            }
        }

        return Result.success();
    }


}
