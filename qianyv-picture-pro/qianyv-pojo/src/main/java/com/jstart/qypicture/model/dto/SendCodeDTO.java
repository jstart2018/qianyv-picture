package com.jstart.qypicture.model.dto;

import lombok.Data;

/**
 * 发送验证码dto
 */
@Data
public class SendCodeDTO {

    /**
     * 邮箱
     */
    private String email;

    /**
     * 手机号
     */
    private String phone;

}
