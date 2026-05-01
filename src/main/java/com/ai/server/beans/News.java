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
@TableName("news") // 优化1：补上表名注解
public class News implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @TableField("title")
    private String title;

    @TableField("description")
    private String description;

    // 优化提示：如果正文是富文本（带HTML标签），在Java中用String没问题。
    // 但在数据库中，建议将类型设置为 LONGTEXT，防止文章过长被截断。
    @TableField("content")
    private String content;

    @TableField("cover")
    private String cover;

    @TableField("count")
    private Integer count = 0; // 优化：默认阅读量为0

    // 优化2：String 改为 LocalDateTime
    @TableField("create_time") // 建议加上下划线映射
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    // 优化3：String 改为 Integer，加上逻辑删除
    @TableField("del")
    @TableLogic
    private Integer del = 0;

    // ================= 以下为优化新增字段 =================

    // 新增：发布者ID（关联 members 表的 id）
    // 如果数据库中没有 author_id 字段，需要添加 exist = false
    @TableField(value = "author_id")
    private Integer authorId;

    // 新增：发布者姓名（仅用于前端展示，不存数据库）
    @TableField(exist = false)
    private String authorName;
}
