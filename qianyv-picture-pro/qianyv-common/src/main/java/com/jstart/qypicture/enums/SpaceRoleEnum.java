package com.jstart.qypicture.enums;

import lombok.Getter;

@Getter
public enum SpaceRoleEnum {
    CREATOR(0, "空间创建者"),
    ADMIN(1, "管理员"),
    EDITOR(2, "编辑者"),
    VIEWER(3, "浏览者"),
    ;


    private final int value;
    private final String desc;


    private SpaceRoleEnum(int value, String desc) {
        this.value = value;
        this.desc = desc;
    }


    public static SpaceRoleEnum getEnumByValue(int value) {
        for (SpaceRoleEnum spaceLevelEnum : values()) {
            if (spaceLevelEnum.getValue() == value) {
                return spaceLevelEnum;
            }
        }
        return null;
    }


}
