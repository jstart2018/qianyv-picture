package com.jstart.qypicture.model.dto;

import lombok.Data;

/**
 * 验证码登录dto
 */
@Data
public class UserLoginByCodeDTO {

    private String account;

    private String code;
}