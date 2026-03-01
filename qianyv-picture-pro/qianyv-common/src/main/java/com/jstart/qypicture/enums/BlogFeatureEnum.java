package com.jstart.qypicture.enums;

import lombok.Getter;

@Getter
public enum BlogFeatureEnum {
    NORMAL(0, "普通"),
    FEATURE(1, "精选"),
    ;


    private final int value;
    private final String desc;


    private BlogFeatureEnum(int value, String desc) {
        this.value = value;
        this.desc = desc;
    }


    public static BlogFeatureEnum getEnumByValue(int value) {
        for (BlogFeatureEnum spaceLevelEnum : values()) {
            if (spaceLevelEnum.getValue() == value) {
                return spaceLevelEnum;
            }
        }
        return null;
    }


}
