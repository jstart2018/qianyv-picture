package com.jstart.qypicture.auth;

import lombok.Getter;

/**
 * 角色枚举
 */
@Getter
public enum SpaceRoleEnum {
    CREATOR(0,"creator"),
    ADMIN(1,"admin"),
    EDITOR(2, "editor"),
    VIEWER(3,"viewer");

    private final Integer key;
    private final String value;

    SpaceRoleEnum(Integer key, String value) {
        this.key = key;
        this.value = value;
    }

    public static SpaceRoleEnum getByKey(Integer key) {
        for (SpaceRoleEnum role : SpaceRoleEnum.values()) {
            if (role.getKey().equals(key)) {
                return role;
            }
        }
        return null;
    }

}