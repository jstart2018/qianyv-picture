package com.jstart.qypicture.model.dto;

import com.jstart.qypicture.model.PageRequest;
import lombok.Data;

import java.util.List;

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
     * 图片thumb_url
     */
    private String thumbUrl;

    /**
     * 关联博客id
     */
    private Long blogId;

    /**
     * 搜索文本
     */
    private String searchText = "";

    /**
     * 分类id
     */
    private Long categoryId;

    /**
     * 是否精选：0-否; 1-是
     */
    private Integer isRecommend;

    /**
     * 图片类型，横屏/竖屏
     */
    private Integer pictureType;

    /**
     * 审核状态
     */
    private Integer reviewStatus;


    /**
     * 空间id，非必须
     */
    private Long spaceId;

}