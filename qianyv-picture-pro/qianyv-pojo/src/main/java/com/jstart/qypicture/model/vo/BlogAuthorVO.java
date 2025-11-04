package com.jstart.qypicture.model.vo;

import lombok.Data;

import java.util.Date;

/**
 * 用户
 */
@Data
public class BlogAuthorVO {
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
     * 性别：0-保密,1-男,2-女
     */
    private Integer gender;

    /**
     * 标签
     */
    private String tag;

    /**
     * 用户签名
     */
    private String introduction;

}