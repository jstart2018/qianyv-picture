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
     * 缩略图 url
     */
    private String thumbUrl;

    /**
     * 标签（JSON 数组）
     */
    private String tags;

    /**
     * 收藏数量
     */
    private Long collectCount;

}