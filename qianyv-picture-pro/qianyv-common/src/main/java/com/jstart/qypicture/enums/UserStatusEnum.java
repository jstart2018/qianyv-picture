package com.jstart.qypicture.enums;

import lombok.Getter;

@Getter
public enum UserStatusEnum {
    DISABLE(0, "禁用"),
    NORMAL(1, "正常"),
    ;


    private final int value;
    private final String desc;


    private UserStatusEnum(int value, String desc) {
        this.value = value;
        this.desc = desc;
    }


    public static UserStatusEnum getEnumByValue(int value) {
        for (UserStatusEnum spaceLevelEnum : values()) {
            if (spaceLevelEnum.getValue() == value) {
                return spaceLevelEnum;
            }
        }
        return null;
    }


}
