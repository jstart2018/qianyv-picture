package com.jstart.qypicture.service.impl;


import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.ObjUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jstart.qypicture.auth.SpaceRoleEnum;
import com.jstart.qypicture.enums.ResultEnum;
import com.jstart.qypicture.enums.SpaceLevelEnum;
import com.jstart.qypicture.exception.BusinessException;
import com.jstart.qypicture.mapper.SpaceMapper;
import com.jstart.qypicture.model.dto.SpaceQueryDTO;
import com.jstart.qypicture.model.entity.Space;
import com.jstart.qypicture.model.entity.SpaceUser;
import com.jstart.qypicture.model.vo.SpaceVO;
import com.jstart.qypicture.service.SpaceService;
import com.jstart.qypicture.service.SpaceUserService;
import com.jstart.qypicture.service.UserService;
import com.jstart.qypicture.utils.ThrowUtils;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author 28435
 * @description 针对表【space(空间)】的数据库操作Service实现
 * @createDate 2025-10-03 08:16:47
 */
@Service
public class SpaceServiceImpl extends ServiceImpl<SpaceMapper, Space>
        implements SpaceService {

    //锁对象
    private static final ConcurrentHashMap<Long, Object> userLockMap = new ConcurrentHashMap<>();

    @Resource
    private UserService userService;
    @Resource
    @Lazy
    private SpaceUserService spaceUserService;
    //编程式事务管理器
    @Resource
    private TransactionTemplate transactionTemplate;
    @Resource
    private SpaceMapper spaceMapper;

    /**
     * 创建空间
     *
     * @return
     */
    @Override
    public long createSpace() {

        //1、获取登录用户校验权限
        Long userId = StpUtil.getLoginIdAsLong();

        //4、填充信息
        Space space = new Space();
        space.setUserId(userId);
        space.setSpaceLevel(SpaceLevelEnum.COMMON.getValue()); //默认普通空间
        space.setSpaceName(String.format("%s的%s", userService.getById(userId).getNickname(), "空间"));
        this.validSpace(space, true);
        this.fillSpaceBySpaceLevel(space);

        // 6、为当前用户获取一个锁对象，在锁内完成事务提交，防止并发问题
        Object lock = userLockMap.computeIfAbsent(userId, k -> new Object());
        synchronized (lock) {
            Long newSpaceId = transactionTemplate.execute(status -> {
                try {
                    // 先查询数据库确认该用户是否已经有对应空间
                    if (this.lambdaQuery()
                            .eq(Space::getUserId, userId)
                            .exists()) {
                        throw new BusinessException(ResultEnum.PARAMS_ERROR, "只能创建一个空间");
                    }
                    // 创建空间
                    boolean saveResult = this.save(space);
                    ThrowUtils.throwIf(!saveResult, ResultEnum.OPERATION_ERROR);

                    //为创建者设置为'创建者'身份
                    SpaceUser spaceUser = new SpaceUser();
                    spaceUser.setUserId(userId);
                    spaceUser.setSpaceId(space.getId());
                    spaceUser.setSpaceRole(SpaceRoleEnum.CREATOR.getKey());
                    boolean saveUserSaveResult = spaceUserService.save(spaceUser);
                    ThrowUtils.throwIf(!saveUserSaveResult, ResultEnum.OPERATION_ERROR, "添加空间成员关系失败");
                    return space.getId();
                } finally {
                    // 清理锁对象，防止内存泄漏
                    userLockMap.remove(userId);
                }
            });
            //return newSpaceId;
            return Optional.ofNullable(newSpaceId).orElse(-1L);
        }
    }

    /**
     * 更换空间名称
     *
     * @param id        空间id
     * @param spaceName 新名称
     * @return 是否成功
     */
    @Override
    public boolean editSpace(Long id, String spaceName) {
        ThrowUtils.throwIf(id == null || StringUtils.isBlank(spaceName), ResultEnum.PARAMS_ERROR);
        return lambdaUpdate()
                .set(Space::getSpaceName, spaceName)
                .eq(Space::getId, id)
                .update();

    }

    /**
     * 空间升级
     *
     * @param spaceId 空间id
     * @param level   升级后的等级
     * @return 是否成功
     */
    @Override
    public boolean upgradeSpace(Long spaceId, Integer level) {
        Space space = this.getById(spaceId);
        ThrowUtils.throwIf(space == null, ResultEnum.PARAMS_ERROR, "空间不存在");
        space.setSpaceLevel(level);
        this.validSpace(space, false);//校验参数
        fillSpaceBySpaceLevel(space);
        return updateById(space);
    }


    /**
     * 校验图片空间数据
     *
     * @param space 空间对象
     * @param add   是否为添加操作
     */
    @Override
    public void validSpace(Space space, boolean add) {
        ThrowUtils.throwIf(space == null, ResultEnum.PARAMS_ERROR);
        // 从对象中取值
        Long id = space.getId();
        String spaceName = space.getSpaceName();
        Integer spaceLevel = space.getSpaceLevel();
        SpaceLevelEnum spaceLevelEnum = SpaceLevelEnum.getEnumByValue(spaceLevel);

        // 修改数据时，id 不能为空，有参数则校验
        if (!add) {
            ThrowUtils.throwIf(ObjUtil.isNull(id), ResultEnum.PARAMS_ERROR, "id 不能为空");
        }
        if (StringUtils.isBlank(spaceName)) {
            throw new BusinessException(ResultEnum.PARAMS_ERROR, "空间名称不能为空");
        }
        ThrowUtils.throwIf(spaceLevelEnum == null, ResultEnum.PARAMS_ERROR, "空间等级不能为空");
        if (spaceLevel != null && spaceLevelEnum == null) {
            throw new BusinessException(ResultEnum.PARAMS_ERROR, "没有该空间等级");
        }
    }


    @Override
    public List<SpaceVO> getUserSpaceInfoList(Long spaceId, Long userId, HashSet<Integer> spaceRole) {
        List<SpaceVO> userSpaceList = spaceMapper.getUserSpaceList(spaceId, userId, spaceRole);
        for (SpaceVO spaceVO : userSpaceList) {
            //获取成员数量
            spaceVO.setMemberCount(spaceUserService.memberCountInSpace(spaceVO.getId(),null));
        }

        return userSpaceList;

    }

    @Override
    public QueryWrapper<Space> getQueryWrapper(SpaceQueryDTO spaceQueryDTO) {
        QueryWrapper<Space> queryWrapper = new QueryWrapper<>();
        if (spaceQueryDTO == null) {
            return queryWrapper;
        }
        //从请求实体中获取 要查询的数据，一个一个塞
        Long id = spaceQueryDTO.getId();
        Long userId = spaceQueryDTO.getUserId();
        String spaceName = spaceQueryDTO.getSpaceName();
        Integer spaceLevel = spaceQueryDTO.getSpaceLevel();
        String sortField = spaceQueryDTO.getSortField();
        String sortOrder = spaceQueryDTO.getSortOrder();

        queryWrapper.eq(ObjUtil.isNotNull(id), "id", id);
        queryWrapper.eq(ObjUtil.isNotNull(userId), "userId", userId);
        queryWrapper.like(StringUtils.isNotBlank(spaceName), "spaceName", spaceName);
        queryWrapper.eq(ObjUtil.isNotNull(spaceLevel), "spaceLevel", spaceLevel);
        queryWrapper.eq(ObjUtil.isNotNull(id), "id", id);

        queryWrapper.orderBy(StringUtils.isNotBlank(sortField), sortOrder.equals("ascend"), sortField);
        return queryWrapper;
    }


    /**
     * 根据空间等级填充空间容量，也可自定义容量
     *
     * @param space 空间等级参数信息
     */
    @Override
    public void fillSpaceBySpaceLevel(Space space) {
        ThrowUtils.throwIf(space == null, ResultEnum.PARAMS_ERROR, "空间对象不能为空");
        ThrowUtils.throwIf(space.getSpaceLevel() == null, ResultEnum.PARAMS_ERROR, "空间级别错误");
        SpaceLevelEnum spaceLevelEnum = SpaceLevelEnum.getEnumByValue(space.getSpaceLevel());
        ThrowUtils.throwIf(spaceLevelEnum == null, ResultEnum.PARAMS_ERROR, "没有该空间级别");

    }

}




