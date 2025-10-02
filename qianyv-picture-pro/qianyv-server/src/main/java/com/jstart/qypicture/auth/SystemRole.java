package com.jstart.qypicture.auth;

import lombok.Getter;

/**
 * 角色枚举
 */
@Getter
public enum SystemRole {
    BOSS(0,"boss"),
    ADMIN(1,"admin"),
    user(2,"user");

    private final Integer key;
    private final String roleName;

    SystemRole(Integer key,String roleName) {
        this.key = key;
        this.roleName = roleName;
    }

    public static SystemRole getByKey(Integer key) {
        for (SystemRole role : SystemRole.values()) {
            if (role.getKey().equals(key)) {
                return role;
            }
        }
        return null;
    }

}