package com.ai.server.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum AdminRoleEnum {
    SUPER_ADMIN("super_admin", "TOPADMIN"),
    NORMAL_ADMIN("normal_admin", "ADMIN");

//    @EnumValue // 标记存入数据库的值
    private final String code;
    @EnumValue
    @JsonValue
    private final String desc;

    AdminRoleEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
