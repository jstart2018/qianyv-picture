package com.jstart.qypicture.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 评论表
 * @TableName common
 */
@TableName(value ="Comment")
@Data
public class Comment {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 关联博客 id
     */
    private Long blogId;

    /**
     * 评论用户 id
     */
    private Long userId;

    /**
     * 父评论 id，null 表示一级评论
     */
    private Long parentId;

    /**
     * 回复的用户 id，null 表示回复帖子本身
     */
    private Long replyUserId;

    /**
     * 点赞数
     */
    private Integer likeCount;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 删除人id
     */
    private Long deleteUser;

    /**
     * 逻辑删除：null-正常, 非null-删除时间
     */
    private Date deleteTime;
}