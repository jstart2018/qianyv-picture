package com.jstart.qypicture.ai.enums;

import lombok.Getter;

@Getter
public enum PictureSizeEnum {

    AAA("16:9","1664*928"),
    BBB("4:3","1472*1140"),
    CCC("1:1","1328*1328"),
    DDD("3:4","1140*1472"),
    EEE("9:16","928*1664");

    private final String scale;
    private final String size;

    PictureSizeEnum(String scale,String size) {
        this.scale = scale;
        this.size = size;
    }

    public static String getSizeByScale(String scale){
        for (PictureSizeEnum pictureSizeEnum : PictureSizeEnum.values()) {
            if(pictureSizeEnum.getScale().equals(scale)){
                return pictureSizeEnum.getSize();
            }
        }
        return CCC.getSize(); // 默认返回1:1的尺寸
    }

}
