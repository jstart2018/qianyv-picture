package com.jstart.qypicture.model.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户编辑请求
 */
@Data
public class SpaceUpgradeDTO implements Serializable {

    private static final long serialVersionUID = 2958041504149997183L;

    //空间id
    private Long id;

    //空间名称
    private Integer level;


}
