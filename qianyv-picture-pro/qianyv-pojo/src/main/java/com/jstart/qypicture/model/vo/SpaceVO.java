package com.jstart.qypicture.model.vo;


import com.jstart.qypicture.model.entity.Space;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 空间
 *
 * @TableName space
 */
@Data
public class SpaceVO implements Serializable {
    private static final long serialVersionUID = 7841082564759766321L;
    /**
     * id
     */
    private Long id;

    /**
     * 空间名称
     */
    private String spaceName;

    /**
     * 空间级别：0-普通版 1-专业版 2-旗舰版
     */
    private Integer spaceLevel;

    /**
     * 空间图片的最大总大小
     */
    private Long maxSize;

    /**
     * 空间图片的最大数量
     */
    private Long maxCount;

    /**
     * 当前空间下图片的总大小
     */
    private Long totalSize;

    /**
     * 当前空间下的图片数量
     */
    private Long totalCount;

    /**
     * 创建用户 id
     */
    private Long userId;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 编辑时间
     */
    private LocalDateTime editTime;

    /**
     * 创建的用户
     */
    private UserVO userVO;

    /**
     * 权限列表
     */
    private List<String> permissionList = new ArrayList<>();


    public static Space VOToObj(SpaceVO spaceVO) {
        if (spaceVO == null) return null;

        Space space = new Space();
        BeanUtils.copyProperties(spaceVO, space);
        return space;
    }

    public static SpaceVO objToVO(Space space) {
        if (space == null) return null;

        SpaceVO spaceVO = new SpaceVO();
        BeanUtils.copyProperties(space, spaceVO);
        return spaceVO;
    }


}