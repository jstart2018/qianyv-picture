package com.jstart.qypicture.model.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 图片表
 *
 * @TableName pub_picture
 */
@Data
public class PictureListVO {

    /**
     * id
     */
    private Long id;

    /**
     * id
     */
    private Long blogId;

    /**
     * id
     */
    private Long spaceId;

    /**
     * 缩略图 url
     */
    private String thumbUrl;

    /**
     * 标签（JSON 数组）
     */
    private String tags;

    /**
     * AI 描述
     */
    private String aiDesc;

    /**
     * 分类id
     */
    private Long categoryId;

    /**
     * 是否精选 0-否; 1-是
     */
    private Integer isRecommend;

    /**
     * 宽高比
     */
    private Double picScale;


    /**
     * 收藏数量
     */
    private Long collectCount;


    /**
     * 审核状态：0-待审核; 1-通过; 2-拒绝
     */
    private Integer reviewStatus;

    /**
     * 审核信息
     */
    private String reviewMessage;

    /**
     * 审核人 ID
     */
    private Long reviewerId;

    /**
     * 审核时间
     */
    private Date reviewTime;

    /**
     * 图片简介
     */
    private String introduction;

}