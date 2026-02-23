package com.jstart.qypicture.model.vo;

import lombok.Data;

import java.util.Date;

/**
 * 简化版博客VO（不包含关联图片）
 */
@Data
public class BlogSimpleVO {
    /**
     * 文章ID
     */
    private Long id;

    /**
     * 作者ID
     */
    private Long userId;

    /**
     * 文章标题
     */
    private String title;

    /**
     * 文章内容
     */
    private String content;

    /**
     * 浏览量
     */
    private Integer viewCount;

    /**
     * 点赞数
     */
    private Integer likeCount;

    /**
     * 评论数
     */
    private Long commentCount;

    /**
     * 收藏数量
     */
    private Long collectCount;

    /**
     * 是否精选：0-否; 1-是
     */
    private Integer isRecommend;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 博客作者
     */
    private BlogAuthorVO user;
}
