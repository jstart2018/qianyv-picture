package com.jstart.qypicture.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jstart.qypicture.model.entity.Space;
import com.jstart.qypicture.model.vo.SpaceVO;

import java.util.List;
import java.util.Set;

/**
 * @author 28435
 * @description 针对表【space(空间)】的数据库操作Mapper
 * @createDate 2025-10-03 08:16:47
 * @Entity com.jstart.qypicture.model.entity.Space
 */
public interface SpaceMapper extends BaseMapper<Space> {


    /**
     * 获取用户所有的空间列表，可只看拥有某个角色的空间
     * @param spaceId 只看某个空间
     * @param loginIdAsLong 查看某个用户所有空间
     * @param spaceRole 拥有该角色的空间
     * @return
     */
    List<SpaceVO> getUserSpaceList(Long spaceId, Long loginIdAsLong, Set<Integer> spaceRole);




}




