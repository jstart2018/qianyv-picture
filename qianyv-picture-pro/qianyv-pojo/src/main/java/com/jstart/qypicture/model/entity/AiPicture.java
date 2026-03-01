package com.jstart.qypicture.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 图片信息表
 * @TableName ai_picture
 */
@TableName(value ="ai_picture")
@Data
public class AiPicture {
    /**
     * 主键ID，自增唯一标识
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 图片存储URL（支持长链接，最大512字符）
     */
    private String url;

    /**
     * 关联的用户ID（与用户表主键关联）
     */
    private Long userId;

    /**
     * 空间id（null表示用户个人图片）
     */
    private Long spaceId;

    /**
     * 创建时间（默认当前时间，无需手动插入）
     */
    private Date createTime;

    /**
     * 状态：0=生成，1=已转移
     */
    private Integer status;

    /**
     * 逻辑删除：null-正常, 非null-删除时间
     */
    private Date deleteTime;
}