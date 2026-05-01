package com.ai.server.beans;

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
@TableName("leavemsg")
public class Leavemsg implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @TableField("member_id")
    private Integer memberId;

    // 优化建议：虽然保留 exist=false 可以跑，但更规范的做法是把这些非数据库字段移到单独的 DTO 中。
    // 这里为了兼容你的原代码，保留并加上注释说明。
    // 留言人扩展字段
    @TableField(exist = false)
    private String memberName;

    @TableField("content")
    private String content;

    // 优化1：String 改为 LocalDateTime，配合 JsonFormat 返回给前端
    @TableField("create_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    @TableField("reply")
    private String reply;

    // 优化1：同上，改为 LocalDateTime
    @TableField("reply_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime replyTime;

    @TableField("reply_id")
    private Integer replyId;

    @TableField(exist = false)
    private String replyName;

    // 优化2：String 改为 Integer，并加上逻辑删除注解
    @TableField("del")
    @TableLogic
    private Integer del = 0;

    // 优化3：新增留言状态字段 (0-待审核, 1-已通过, 2-已拒绝)
    @TableField("status")
    private Integer status = 1;
}
