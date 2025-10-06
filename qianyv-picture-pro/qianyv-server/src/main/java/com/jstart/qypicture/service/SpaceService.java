package com.jstart.qypicture.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jstart.qypicture.model.dto.SpaceQueryDTO;
import com.jstart.qypicture.model.dto.SpaceUpgradeDTO;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jstart.qypicture.model.entity.Space;
import com.jstart.qypicture.model.vo.SpaceVO;

/**
 * @author 28435
 * @description 针对表【space(空间)】的数据库操作Service
 * @createDate 2025-10-03 08:16:47
 */
public interface SpaceService extends IService<Space> {


    /**
     * 创建空间
     */
    long createSpace();


    /**
     * 更改空间名称
     */
    boolean editSpace(Long id,String spaceName);

    /**
     * 空间升级
     */
    boolean upgradeSpace(Long spaceId, Integer level);


    /**
     * 校验图片空间数据，更新时使用
     */
    public void validSpace(Space space, boolean add);

    /**
     * 构建Space的查询条件
     */
    QueryWrapper<Space> getQueryWrapper(SpaceQueryDTO spaceQueryRequest);

    /**
     * 获取空间vo
     */
    SpaceVO getSpaceVO(Space space);


    /**
     * 根据空间级别填充空间容量
     */
    void fillSpaceBySpaceLevel(Space space);

}
