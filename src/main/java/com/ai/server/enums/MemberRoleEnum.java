package com.ai.server.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum MemberRoleEnum {


    PUBLISHER(1, "ISSUER"),
    VOLUNTEER(2, "TENANT");


    private final Integer code;

    @EnumValue
    @JsonValue
    private final String desc;

    MemberRoleEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
