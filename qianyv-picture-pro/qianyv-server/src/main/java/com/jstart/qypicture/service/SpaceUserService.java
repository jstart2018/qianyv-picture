package com.jstart.qypicture.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jstart.qypicture.auth.SpaceRoleEnum;
import com.jstart.qypicture.model.dto.SpaceUserAddDTO;
import com.jstart.qypicture.model.dto.SpaceUserEditDTO;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jstart.qypicture.model.dto.SpaceUserQueryDTO;
import com.jstart.qypicture.model.entity.SpaceUser;
import com.jstart.qypicture.model.vo.SpaceUserVO;

import java.util.List;
import java.util.Set;

/**
 * @author 28435
 * @description 针对表【space_user(空间用户关联)】的数据库操作Service
 * @createDate 2025-10-03 08:16:50
 */
public interface SpaceUserService extends IService<SpaceUser> {

    /**
     * 添加空间成员关系数据
     */
    long addSpaceUser(SpaceUserAddDTO spaceUserAddDTO);

    Long memberCountInSpace(Long spaceId, Set<Integer> roleSet);

    /**
     * 踢出成员
     */
    boolean kickOutMember(Long spaceId, Long userId);

    /**
     * 获取空间成员列表
     */
    Page<SpaceUserVO> listSpaceUsers(SpaceUserQueryDTO spaceUserQueryDTO);

    /**
     * 修改成员角色
     */
    boolean editUserRole(SpaceUserEditDTO spaceUserEditDTO);

    /**
     * 构建查询条件
     */
    QueryWrapper<SpaceUser> getQueryWrapper(SpaceUser spaceUser);

    /**
     * 获取空间用户关系信息集合
     */
    List<SpaceUserVO> getSpaceUserVOList(List<SpaceUser> spaceUserList);

    SpaceUser getUserRoleInSpace(long loginIdAsLong, Long spaceId);
}
