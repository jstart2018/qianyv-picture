package com.jstart.qypicture.model.dto;

import lombok.Data;

import java.util.Date;

/**
 * 博客表
 *
 * @TableName blog
 */
@Data
public class BlogAuditDTO {
    /**
     * 文章ID
     */
    private Long id;

    /**
     * 审核状态：0-待审核; 1-通过; 2-拒绝
     */
    private Integer reviewStatus;

    /**
     * 审核信息
     */
    private String reviewMessage;

    /**
     * 审核时间
     */
    private Date reviewTime;

}