package com.jstart.qypicture.enums;

import lombok.Getter;

@Getter
public enum BlogAuditEnum {
    WAITING(0, "待审核"),
    PASS(1, "通过"),
    REJECT(2, "拒绝"),
    ;


    private final int value;
    private final String desc;


    private BlogAuditEnum(int value, String desc) {
        this.value = value;
        this.desc = desc;
    }


    public static BlogAuditEnum getEnumByValue(int value) {
        for (BlogAuditEnum spaceLevelEnum : values()) {
            if (spaceLevelEnum.getValue() == value) {
                return spaceLevelEnum;
            }
        }
        return null;
    }


}
