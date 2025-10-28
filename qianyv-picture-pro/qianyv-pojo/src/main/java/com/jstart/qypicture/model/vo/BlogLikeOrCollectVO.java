package com.jstart.qypicture.model.vo;

import lombok.Data;

/**
 * @TableName blog
 */
@Data
public class BlogLikeOrCollectVO {

    /**
     * 点赞或收藏数量
     */
    private Long count;

    /**
     * 当前用户是否已点赞或收藏
     */
    private Boolean haveBean;

}