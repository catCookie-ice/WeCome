package com.ai.server.beans;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor//自动生成无参构造器和全参构造器。
@TableName("activity_application")//指定这个类映射到数据库中的 activity_application 表。
public class ActivityApplication {

    @TableId(value = "id", type = IdType.AUTO)//标记 id 为主键，并且主键的生成策略是数据库自增（AUTO_INCREMENT）。
    private Integer id;

    @TableField("activity_id")
    private Integer activityId;

    @TableField("volunteer_id")//指定 Java 属性名与数据库列名的映射关系（例如 Java 里的 userId 映射到数据库的 volunteer_id 列）。
    private Integer userId;

    @TableField("status")//
    private String status; // pending, approved, rejected

    @TableField("apply_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime applyTime;

    @TableField("review_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime reviewTime;

    @TableField("reviewer_id")
    private Integer reviewerId;

    @TableField("del")
    @TableLogic//标记逻辑删除字段。在执行删除操作时，MyBatis-Plus 不会真删除，而是执行 UPDATE SET del = 1；查询时会自动加上 WHERE del = 0。
    private Integer del = 0;

    @TableField("created_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")//这是 Jackson 库的注解。当把这个对象转换成 JSON 返回给前端时，会自动把 LocalDateTime 格式化成 年-月-日 时:分:秒 的形式，并处理东八区时区，防止时间差 8 小时。
    private LocalDateTime createdAt;

    @TableField("updated_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updatedAt;
}
