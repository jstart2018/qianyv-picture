package com.jstart.qypicture.model.dto;

import lombok.Data;

import java.util.Date;

/**
 * 图片表
 *
 * @TableName pub_picture
 */
@Data
public class PictureEditDTO {
    /**
     * 图片id
     */
    private Long id;

    /**
     * 关联博客id
     */
    private Long blogId;

    /**
     * 简介
     */
    private String introduction;

    /**
     * 分类id
     */
    private Long categoryId;

    /**
     * 标签（JSON 数组）
     */
    private String tags;

    /**
     * 空间id，非必须
     */
    private Long spaceId;

}