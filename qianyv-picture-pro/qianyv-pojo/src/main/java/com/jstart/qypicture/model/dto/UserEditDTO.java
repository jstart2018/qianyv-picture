package com.jstart.qypicture.model.dto;

import lombok.Data;

/**
 * 用户dto
 */
@Data
public class UserEditDTO {

    /**
     * 用户昵称（最长10汉字）
     */
    private String nickname;

    /**
     * 性别：0-保密,1-男,2-女
     */
    private Integer gender;

    /**
     * 用户简介
     */
    private String introduction;

}