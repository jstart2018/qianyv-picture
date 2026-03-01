package com.jstart.qypicture.model.dto;

import lombok.Data;

/**
 * 修改密码请求DTO
 */
@Data
public class ChangePasswordDTO {

    /**
     * 验证方式：email-邮箱, phone-手机号
     */
    private String type;

    /**
     * 验证码
     */
    private String code;

    /**
     * 新密码（16位以下，需要数字和字母组合）
     */
    private String newPassword;
}
