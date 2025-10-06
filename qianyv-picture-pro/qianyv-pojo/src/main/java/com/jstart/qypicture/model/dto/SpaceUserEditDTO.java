package com.jstart.qypicture.model.dto;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 空间用户关联
 *
 * @TableName space_user
 */
@TableName(value = "space_user")
@Data
public class SpaceUserEditDTO implements Serializable {

    /**
     * 空间成员id
     */
    Long userId;

    /**
     * 空间id
     */
    Long spaceId;

    /**
     * 空间角色：viewer/editor/admin
     */
    private Integer spaceRole;


    private static final long serialVersionUID = 1L;
}