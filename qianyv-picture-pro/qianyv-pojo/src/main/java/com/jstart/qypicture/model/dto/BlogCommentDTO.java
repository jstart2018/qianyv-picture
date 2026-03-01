package com.jstart.qypicture.model.dto;

import lombok.Data;

/**
 * @TableName blog
 */
@Data
public class BlogCommentDTO {

    /**
     * 博客id
     */
    private Long id;

    /**
     * 父评论id
     */
    private Long parentId;

    /**
     * 评论内容
     */
    private String content;

}