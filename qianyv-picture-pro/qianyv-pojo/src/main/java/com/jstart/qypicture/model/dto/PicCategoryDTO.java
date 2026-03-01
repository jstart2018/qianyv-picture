package com.jstart.qypicture.model.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 图片分类表
 *
 * @TableName pic_category
 */
@Data
public class PicCategoryDTO {
    /**
     * 分类id
     */
    private Long id;

    /**
     * 分类名称（如“魅力/迷人”“自然/风景”）
     */
    private String categoryName;

    /**
     * 是否升序，用于查询
     */
    private Boolean isAsc;

}