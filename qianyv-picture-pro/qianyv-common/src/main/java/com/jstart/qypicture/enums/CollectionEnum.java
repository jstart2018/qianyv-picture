package com.jstart.qypicture.enums;

import lombok.Getter;

@Getter
public enum CollectionEnum {
    PICTURE(0, "图片"),
    BLOG(1, "博客");


    private final int value;
    private final String desc;


    private CollectionEnum(int value, String desc) {
        this.value = value;
        this.desc = desc;
    }


    public static CollectionEnum getEnumByValue(int value) {
        for (CollectionEnum spaceLevelEnum : values()) {
            if (spaceLevelEnum.getValue() == value) {
                return spaceLevelEnum;
            }
        }
        return null;
    }


}
