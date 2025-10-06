package com.jstart.qypicture.auth;

import lombok.Getter;

/**
 * 角色枚举
 */
@Getter
public enum SystemRoleEnum {
    BOSS(0,"boss"),
    ADMIN(1,"admin"),
    user(2,"user");

    private final Integer key;
    private final String value;

    SystemRoleEnum(Integer key, String value) {
        this.key = key;
        this.value = value;
    }

    public static SystemRoleEnum getByKey(Integer key) {
        for (SystemRoleEnum role : SystemRoleEnum.values()) {
            if (role.getKey().equals(key)) {
                return role;
            }
        }
        return null;
    }

}