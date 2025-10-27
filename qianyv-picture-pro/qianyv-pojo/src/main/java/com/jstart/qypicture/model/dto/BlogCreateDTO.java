package com.jstart.qypicture.model.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 博客表
 *
 * @TableName blog
 */
@TableName(value = "blog")
@Data
public class BlogCreateDTO {

    /**
     * 文章标题
     */
    private String title;

    /**
     * 文章内容
     */
    private String content;

    /**
     * 图片id集合
     */
    private List<PictureEditDTO> pictureEditDTOList;

}