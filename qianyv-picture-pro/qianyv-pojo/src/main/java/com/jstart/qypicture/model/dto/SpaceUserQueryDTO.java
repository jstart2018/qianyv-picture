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
public class SpaceUserQueryDTO implements Serializable {

    /**
     * 空间成员关系id
     */
    Long id;

    /**
     * 空间id
     */
    private Long spaceId;

    /**
     * 用户 id
     */
    private Long userId;


    /**
     * 空间角色：viewer/editor/admin
     */
    private Integer spaceRole;


    private static final long serialVersionUID = 1L;
}