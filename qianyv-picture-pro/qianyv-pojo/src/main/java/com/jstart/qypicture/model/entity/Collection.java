package com.jstart.qypicture.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 收藏表
 * @TableName collection
 */
@TableName(value ="collection")
@Data
public class Collection {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户 id
     */
    private Long userId;

    /**
     * 收藏目标 id
     */
    private Long targetId;

    /**
     * 内容类型：0-图片; 1-博客
     */
    private Integer contentType;

    /**
     * 创建时间
     */
    private Date createTime;
}