package com.jstart.qypicture.model.dto;

import lombok.Data;

/**
 * 发送验证码dto
 */
@Data
public class SendCodeDTO {

    private enum Type {
        //短信
        SMS,
        //邮箱
        EMAIL
    }

    private Type type;

    private String account; // 或用 email 和 phone 两个字段

}
