package com.jstart.qypicture.model.vo;

import lombok.Data;

/**
 * 用户
 */
@Data
public class UserInfoVO {
    /**
     * 主键id
     */
    private Long id;

    /**
     * 用户昵称
     */
    private String nickname;

    /**
     * 头像URL
     */
    private String avatar;

    /**
     * 标签
     */
    private String tag;

    /**
     * 用户简介
     */
    private String introduction;

    /**
     * 分享数
     */
    private Long shareCount = 0L;

    /**
     * 获赞数
     */
    private Long likeCount = 0L;

    /**
     * 获载数
     */
    private Long downloadCount = 0L;

}