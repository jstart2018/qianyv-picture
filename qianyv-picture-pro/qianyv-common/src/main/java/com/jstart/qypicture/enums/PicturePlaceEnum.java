package com.jstart.qypicture.enums;

public enum PicturePlaceEnum {

    PUBLIC,
    SPACE;


    public static PicturePlaceEnum getManageType(Long spaceId) {
        if (spaceId == null) {
            return PUBLIC;
        } else {
            return SPACE;
        }
    }

}
