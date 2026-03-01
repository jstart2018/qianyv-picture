package com.jstart.qypicture.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jstart.qypicture.model.entity.SpaceUser;

import java.util.Set;

/**
 * @author 28435
 * @description 针对表【space_user(空间用户关联)】的数据库操作Mapper
 * @createDate 2025-10-03 08:16:50
 * @Entity com.jstart.qypicture.model.entity.SpaceUser
 */
public interface SpaceUserMapper extends BaseMapper<SpaceUser> {

    /**
     * 获取空间成员数量
     * @param spaceId 空间id
     * @param roleSet 空间内满足该角色的
     * @return 成员数量
     */
    Long getSpaceMemberCount(Long spaceId, Set<Integer> roleSet);

}




