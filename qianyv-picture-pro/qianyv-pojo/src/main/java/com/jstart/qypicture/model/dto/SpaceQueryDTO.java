package com.jstart.qypicture.model.dto;


import com.jstart.qypicture.model.PageRequest;
import lombok.Data;

import java.io.Serializable;

/**
 * 创建空间请求
 */
@Data
public class SpaceQueryDTO extends PageRequest implements Serializable {

    private static final long serialVersionUID = -8162475754189469907L;

    /**
     * 空间id
     */
    private Long id;

    /**
     * 创建用户 id
     */
    private Long userId;

    /**
     * 空间名称
     */
    private String spaceName;

    /**
     * 空间级别：0-普通版 1-专业版 2-旗舰版
     */
    private Integer spaceLevel;


}
