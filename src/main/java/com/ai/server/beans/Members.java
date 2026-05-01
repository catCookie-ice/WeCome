package com.ai.server.beans;

import com.ai.server.enums.MemberRoleEnum;
import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("members")
public class Members implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @TableField("uname")
    private String uname;

    @TableField("upass")
    private String upass;

    @TableField("nickname")
    private String nickname;

    @TableField("phone")
    private String phone;

    // 优化1：photos 改为 photo（单数），语义更准确
    @TableField("photo")
    private String photo;

    // 优化2：String 改为 LocalDateTime
    @TableField("createtime")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createtime;

    // 优化3：String 改为 Integer，加上逻辑删除注解
    @TableField("del")
    @TableLogic
    private Integer del = 0;

    @TableField("role")
    private MemberRoleEnum role = MemberRoleEnum.PUBLISHER;

    // 扩展字段：如果有些接口需要返回真实姓名，可以加上（按需）
    // @TableField(exist = false)
    // private String realName;
}
