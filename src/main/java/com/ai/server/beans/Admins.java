package com.ai.server.beans;

import com.ai.server.enums.AdminRoleEnum;
import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("admins")
public class Admins {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @TableField("username")
    private String username;

    @TableField("password")
    private String password;

    // 优化1：role 改为枚举类型，保证数据安全性
    @TableField("role")
    private AdminRoleEnum role;

    // 优化2：del 改为 Integer，配合 @TableLogic 完美实现逻辑删除
    @TableField("del")
    @TableLogic
    private Integer del = 0;

    // 优化3：补充审计字段
    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;

    // 注意：密码字段在实际业务中，返回给前端时应该置空或脱敏，
    // 可以在 DTO 里处理，这里实体类保留完整字段用于数据库读写。
}
