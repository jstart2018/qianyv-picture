package com.jstart.qypicture.model.dto;

import com.jstart.qypicture.model.PageRequest;
import lombok.Data;

/**
 * 博客分页查询DTO
 */
@Data
public class BlogPageQueryDTO extends PageRequest {

    /**
     * 作者ID（查询"我发布的"博客时传入）
     */
    private Long userId;

    /**
     * 查询我点赞的博客
     */
    private Boolean myLike;

    /**
     * 查询我收藏的博客
     */
    private Boolean myCollect;

    /**
     * 搜索文本
     */
    private String searchText;

    /**
     * 是否精选：0-否; 1-是
     */
    private Integer isRecommend;

    /**
     * 审核状态：0-待审核; 1-通过; 2-拒绝
     */
    private Integer reviewStatus;
}
