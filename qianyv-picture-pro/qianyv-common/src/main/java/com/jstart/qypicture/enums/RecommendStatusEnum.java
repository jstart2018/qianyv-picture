package com.jstart.qypicture.enums;

import lombok.Getter;

@Getter
public enum RecommendStatusEnum {
    ORDINARY(0, "普通"),
    RECOMMENDED(1, "精选"),
    ;


    private final int value;
    private final String desc;


    private RecommendStatusEnum(int value, String desc) {
        this.value = value;
        this.desc = desc;
    }


    public static RecommendStatusEnum getEnumByValue(int value) {
        for (RecommendStatusEnum spaceLevelEnum : values()) {
            if (spaceLevelEnum.getValue() == value) {
                return spaceLevelEnum;
            }
        }
        return null;
    }


}
