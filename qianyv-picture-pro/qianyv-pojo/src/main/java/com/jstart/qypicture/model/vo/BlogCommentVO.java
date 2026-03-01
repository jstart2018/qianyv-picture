package com.jstart.qypicture.model.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @TableName blog
 */
@Data
public class BlogCommentVO {

    /**
     * 博客id
     */
    private Long id;

    /**
     * 评论id
     */
    private Long commentId;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 评论时间
     */
    private LocalDateTime createTime;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 父评论id，一级评论为 null
     */
    private Long parentId;

    /**
     * 父评论用户名，一级评论为 null
     */
    private String parentUsername;
}