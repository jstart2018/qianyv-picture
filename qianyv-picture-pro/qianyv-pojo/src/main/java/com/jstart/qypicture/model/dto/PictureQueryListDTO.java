package com.jstart.qypicture.model.dto;

import com.jstart.qypicture.model.PageRequest;
import lombok.Data;

/**
 * 图片查询请求
 */
@Data
public class PictureQueryListDTO extends PageRequest {
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