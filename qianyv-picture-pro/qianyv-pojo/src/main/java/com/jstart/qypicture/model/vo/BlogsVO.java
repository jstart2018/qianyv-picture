package com.jstart.qypicture.model.vo;


import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * @TableName blog
 */
@Data
public class BlogsVO {
    /**
     * 文章ID
     */
    private Long id;

    /**
     * 更新时间
     */
    private Date updateTime;

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
     * 图片url集合
     */
    private List<PictureListVO> pictureVOList;

    /**
     * 博客作者
     */
    private BlogAuthorVO user;

}
