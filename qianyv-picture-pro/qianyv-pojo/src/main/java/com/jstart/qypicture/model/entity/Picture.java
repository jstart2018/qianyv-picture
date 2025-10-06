package com.jstart.qypicture.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.util.Date;

import lombok.Data;

/**
 * 图片表
 *
 * @TableName picture
 */
@TableName(value = "picture")
@Data
public class Picture {
    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 创建用户 id
     */
    private Long userId;

    /**
     * 关联博客id
     */
    private Long blogId;

    /**
     * 图片 url
     */
    private String url;

    /**
     * 简介
     */
    private String introduction;

    /**
     * 分类
     */
    private Long categoryId;

    /**
     * 标签（JSON 数组）
     */
    private Object tags;

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
     * 存储类型：0-公共; 1-私有; 2-团队
     */
    private Integer spaceType;

    /**
     * 所属空间id，默认0表示公共空间
     */
    private Long spaceId;

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
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 逻辑删除：null-正常, 非null-删除时间
     */
    private Date deleteTime;
}