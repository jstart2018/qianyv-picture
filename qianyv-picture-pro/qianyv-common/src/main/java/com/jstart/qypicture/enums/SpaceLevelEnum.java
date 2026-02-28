package com.jstart.qypicture.enums;

import lombok.Getter;

@Getter
public enum SpaceLevelEnum {
    FREE(0, "免费版", 50L * 1024 * 1024, 10),//50M，10张图片
    COMMON(1, "普通版", 200L * 1024 * 1024, 50),//200M，50张图片
    PROFESSIONAL(2, "专业版", 1024 * 1024 * 1024, 1000),//1G，1000张图片
    FLAGSHIP(3, "旗舰版", 10L * 1024 * 1024 * 1024, 10000)//10G，10000张图片
    ;


    private final int value;
    private final String desc;
    private final long maxSize;
    private final long maxCount;


    private SpaceLevelEnum(int value, String desc, long maxSize, long maxCount) {
        this.value = value;
        this.desc = desc;
        this.maxSize = maxSize;
        this.maxCount = maxCount;
    }


    public static SpaceLevelEnum getEnumByValue(int value) {
        for (SpaceLevelEnum spaceLevelEnum : values()) {
            if (spaceLevelEnum.getValue() == value) {
                return spaceLevelEnum;
            }
        }
        return null;
    }


}
