package com.jstart.qypicture.model.dto;

import lombok.Data;

/**
 * 评论表
 * @TableName common
 */
@Data
public class CommentAddDTO {

    /**
     * 评论内容
     */
    private String content;

    /**
     * 关联博客 id
     */
    private Long blogId;

    /**
     * 父评论 id，null 表示一级评论
     */
    private Long parentId;

    /**
     * 回复的用户 id，null 表示回复帖子本身
     */
    private Long replyToUserId;

}