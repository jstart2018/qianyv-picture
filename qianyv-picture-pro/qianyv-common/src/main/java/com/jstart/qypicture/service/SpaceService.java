package com.jstart.qypicture.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jstart.qypicture.model.dto.SpaceQueryDTO;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jstart.qypicture.model.dto.SpaceUserQueryDTO;
import com.jstart.qypicture.model.entity.Space;
import com.jstart.qypicture.model.vo.SpaceVO;

import java.util.HashSet;
import java.util.List;

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
    boolean editSpace(Long id, String spaceName);

    /**
     * 空间升级
     */
    boolean upgradeSpace(Long spaceId, Integer level);

    /**
     * 删除空间
     */
    boolean deleteSpace(Long spaceId);

    /**
     * 校验图片空间数据，更新时使用
     */
    void validSpace(Space space, boolean add);

    /**
     * 获取用户所有的空间列表，可只看拥有某个角色的空间
     *
     * @param spaceRole 角色集合
     * @return 空间列表
     */
    List<SpaceVO> getUserSpaceInfoList(Long spaceId, Long userId,  HashSet<Integer> spaceRole);

    /**
     * 构建Space的查询条件
     */
    QueryWrapper<Space> getQueryWrapper(SpaceQueryDTO spaceQueryRequest);


    /**
     * 根据空间级别填充空间容量
     */
    void fillSpaceBySpaceLevel(Space space);


    /**
     * 管理端：分页获取空间列表
     * @param spaceQueryDTO
     * @return
     */
    Page<List<SpaceVO>> listByPage(SpaceQueryDTO spaceQueryDTO);

    /**
     * 空间状态修改
     */
    boolean changeSpaceStatus(Long spaceId, Integer status);

}
