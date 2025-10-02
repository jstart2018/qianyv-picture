package com.jstart.qypicture.model.dto;

import lombok.Data;

/**
 * 密码登录dto
 */
@Data
public class UserLoginByPasswordDTO {

    private String account;

    private String password;
}