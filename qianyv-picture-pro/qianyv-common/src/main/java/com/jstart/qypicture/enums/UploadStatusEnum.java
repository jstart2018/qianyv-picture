package com.jstart.qypicture.enums;

import lombok.Getter;

@Getter
public enum UploadStatusEnum {

    PREVIEW(0, "预览"),
    FORMAL(1, "正式");

    private final Integer key;
    private final String text;

    UploadStatusEnum(Integer key, String text) {
        this.key = key;
        this.text = text;
    }


}
