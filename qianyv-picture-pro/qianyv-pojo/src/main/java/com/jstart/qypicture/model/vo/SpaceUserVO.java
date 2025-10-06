package com.jstart.qypicture.model.vo;


import com.jstart.qypicture.model.PageRequest;
import com.jstart.qypicture.model.entity.SpaceUser;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class SpaceUserVO extends PageRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 空间 id
     */
    private Long spaceId;

    /**
     * 用户 id
     */
    private Long userId;

    /**
     * 用户名称
     */
    private String userName;

    /**
     * 空间角色：viewer/editor/admin
     */
    private String spaceRole;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;



    private static final long serialVersionUID = 1L;

    /**
     * 封装类转对象
     *
     * @param spaceUserVO
     * @return
     */
    public static SpaceUser voToObj(SpaceUserVO spaceUserVO) {
        if (spaceUserVO == null) {
            return null;
        }
        SpaceUser spaceUser = new SpaceUser();
        BeanUtils.copyProperties(spaceUserVO, spaceUser);
        return spaceUser;
    }

    /**
     * 对象转封装类
     *
     * @param spaceUser
     * @return
     */
    public static SpaceUserVO objToVo(SpaceUser spaceUser) {
        if (spaceUser == null) {
            return null;
        }
        SpaceUserVO spaceUserVO = new SpaceUserVO();
        BeanUtils.copyProperties(spaceUser, spaceUserVO);
        return spaceUserVO;
    }
}
