package com.jstart.qypicture.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.util.Date;

import lombok.Data;

/**
 * 图片分类表
 *
 * @TableName pic_category
 */
@TableName(value = "pic_category")
@Data
public class PicCategory {
    /**
     * 分类id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 分类名称（如“魅力/迷人”“自然/风景”）
     */
    private String categoryName;

    /**
     * 排序权重（数字越大越靠前）
     */
    private Integer sort;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}