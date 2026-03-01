package com.jstart.qypicture.ai.enums;

import lombok.Getter;


/**
 * AI行为枚举
 * key：对应的AIClient的bean
 * desc：描述
 */
@Getter
public enum AiActEnum {

    GENERATE_IMG("genImg","生成图片"),
    SEND_IMG("sendImg","推荐图片"),
    INTRO_FUNCTION("introFunction","功能介绍"),
    PARSE_IMG("parseImg","图片解析"),
    NORMAL_CHAT("normalChat","聊天对话");

    private final String name;
    private final String desc;

    AiActEnum(String name,String desc) {
        this.name = name;
        this.desc = desc;
    }

}
