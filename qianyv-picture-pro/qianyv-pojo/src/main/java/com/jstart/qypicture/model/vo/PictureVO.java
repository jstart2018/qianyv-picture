package com.jstart.qypicture.model.vo;

import lombok.Data;

import java.time.LocalDate;

/**
 * 图片表
 * @TableName pub_picture
 */
@Data
public class PictureVO {
    /**
     * id
     */
    private Long id;

    /**
     * 创建用户 id
     */
    private Long userId;

    /**
     * 缩略图 url
     */
    private String thumbUrl;

    /**
     * AI 描述
     */
    private String aiDesc;

    /**
     * 简介
     */
    private String introduction;

    /**
     * 分类id
     */
    private Long categoryId;

    /**
     * 是否精选 0-否; 1-是
     */
    private Integer isRecommend;

    /**
     * 标签（JSON 数组）
     */
    private String tags;

    /**
     * 图片体积
     */
    private Long picSize;

    /**
     * 图片宽度
     */
    private Integer picWidth;

    /**
     * 图片高度
     */
    private Integer picHeight;

    /**
     * 图片宽高比例
     */
    private Double picScale;

    /**
     * 图片格式
     */
    private String picFormat;

    /**
     * 创建时间
     */
    private LocalDate createTime;

    /**
     * 图片获收藏数量
     */
    private Long collectCount = 0L;

    /**
     * 图片获下载数量
     */
    private Long downLoadCount = 0L;


    /**
     * 创建用户
     */
    private UserInfoVO userInfoVO;

}