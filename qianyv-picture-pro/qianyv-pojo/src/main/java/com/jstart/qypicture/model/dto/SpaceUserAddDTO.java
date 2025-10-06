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
public class SpaceUserAddDTO implements Serializable {

    /**
     * 空间 id
     */
    private Long spaceId;

    /**
     * 用户 id
     */
    private Long userId;


    private static final long serialVersionUID = 1L;
}