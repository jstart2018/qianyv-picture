package com.jstart.qypicture.ai.entity;

import java.util.List;

/**
 * AI解析图片后返回的实体（注意模型要支持结构化输出
 */
public class ParsePictureRespond {

    /**
     * 图片的色调
     */
    private String color;

    /**
     * 图片的标签
     */
    private List<String> tags;

    /**
     * 图片的描述
     */
    private String description;

}
