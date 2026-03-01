package com.jstart.qypicture.model.dto;

import com.jstart.qypicture.model.PageRequest;
import lombok.Data;

/**
 * 图片分页查询DTO（支持查询我发布的/我收藏的）
 */
@Data
public class PicturePageQueryDTO extends PageRequest {

    /**
     * 上传用户ID（查询"我发布的"图片时传入）
     */
    private Long userId;

    /**
     * 查询我收藏的图片
     */
    private Boolean myCollect;

    /**
     * 搜索文本
     */
    private String searchText;

    /**
     * 分类id
     */
    private Long categoryId;

    /**
     * 图片类型，0-横屏/1-竖屏
     */
    private Integer pictureType;

    /**
     * 是否精选：0-否; 1-是
     */
    private Integer isRecommend;

    /**
     * 审核状态：0-待审核; 1-通过; 2-拒绝
     */
    private Integer reviewStatus;
}
