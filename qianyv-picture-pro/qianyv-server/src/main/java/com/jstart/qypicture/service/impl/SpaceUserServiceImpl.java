package com.jstart.qypicture.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jstart.qypicture.auth.SpaceRoleEnum;
import com.jstart.qypicture.enums.ResultEnum;
import com.jstart.qypicture.exception.BusinessException;
import com.jstart.qypicture.model.dto.SpaceUserAddDTO;
import com.jstart.qypicture.model.dto.SpaceUserEditDTO;
import com.jstart.qypicture.model.dto.SpaceUserQueryDTO;
import com.jstart.qypicture.model.entity.Space;
import com.jstart.qypicture.model.entity.SpaceUser;
import com.jstart.qypicture.model.entity.User;
import com.jstart.qypicture.model.vo.SpaceUserVO;
import com.jstart.qypicture.service.SpaceService;
import com.jstart.qypicture.service.SpaceUserService;
import com.jstart.qypicture.mapper.SpaceUserMapper;
import com.jstart.qypicture.service.UserService;
import com.jstart.qypicture.utils.ThrowUtils;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author 28435
 * @description 针对表【space_user(空间用户关联)】的数据库操作Service实现
 * @createDate 2025-10-03 08:16:50
 */
@Service
public class SpaceUserServiceImpl extends ServiceImpl<SpaceUserMapper, SpaceUser>
        implements SpaceUserService {

    @Resource
    private UserService userService;
    @Resource
    private SpaceService spaceService;


    /**
     * 添加空间成员 todo: 空间权限：仅空间管理员
     *
     * @param spaceUserAddDTO
     * @return
     */
    @Override
    public long addSpaceUser(SpaceUserAddDTO spaceUserAddDTO) {
        // 1、参数校验
        ThrowUtils.throwIf(spaceUserAddDTO == null, ResultEnum.PARAMS_ERROR);
        SpaceUser spaceUser = new SpaceUser();
        BeanUtils.copyProperties(spaceUserAddDTO, spaceUser);
        //2、校验数据库信息
        ThrowUtils.throwIf(spaceUser == null, ResultEnum.PARAMS_ERROR);
        // 创建时，空间 id 和用户 id 必填
        Long spaceId = spaceUser.getSpaceId();
        Long userId = spaceUser.getUserId();
        ThrowUtils.throwIf(ObjectUtil.hasEmpty(spaceId, userId), ResultEnum.PARAMS_ERROR);
        User user = userService.getById(userId);
        ThrowUtils.throwIf(user == null, ResultEnum.NOT_FOUND_ERROR, "用户不存在");
        Space space = spaceService.getById(spaceId);
        ThrowUtils.throwIf(space == null, ResultEnum.NOT_FOUND_ERROR, "空间不存在");
        // 3、添加成员默认为 viewter
        spaceUser.setSpaceRole(SpaceRoleEnum.VIEWER.getKey());
        // 4、数据库操作
        boolean result = this.save(spaceUser);
        ThrowUtils.throwIf(!result, ResultEnum.OPERATION_ERROR);
        return spaceUser.getId();
    }

    /**
     * 获取空间成员数量
     *
     * @param spaceId 空间 id
     * @param roleSet 统计的角色,null统计所有角色
     * @return
     */
    @Override
    public Long memberCountInSpace(Long spaceId, Set<Integer> roleSet) {
        ThrowUtils.throwIf(ObjUtil.isEmpty(spaceId), ResultEnum.PARAMS_ERROR);
        return this.getBaseMapper().getSpaceMemberCount(spaceId, roleSet);
    }

    /**
     * 移除空间成员 todo: 空间权限：仅空间管理员
     *
     * @param spaceId 空间 id
     * @param userId  用户 id
     * @return 移除是否成功
     */
    @Override
    public boolean kickOutMember(Long spaceId, Long userId) {
        ThrowUtils.throwIf(ObjUtil.isEmpty(spaceId) || ObjUtil.isEmpty(userId), ResultEnum.PARAMS_ERROR);
        SpaceUser spaceUser = new SpaceUser();
        spaceUser.setSpaceId(spaceId);
        spaceUser.setUserId(userId);
        QueryWrapper<SpaceUser> queryWrapper = this.getQueryWrapper(spaceUser);
        SpaceUser result = this.getOne(queryWrapper);
        ThrowUtils.throwIf(result == null, ResultEnum.NOT_FOUND_ERROR, "该空间无该成员记录");
        return this.remove(queryWrapper);
    }

    @Override
    public boolean quitSpace(Long spaceId) {
        Space space = spaceService.getById(spaceId);
        ThrowUtils.throwIf(space == null, ResultEnum.NOT_FOUND_ERROR, "空间不存在");
        if (space.getUserId().equals(StpUtil.getLoginIdAsLong())) {
            throw new BusinessException(ResultEnum.OPERATION_ERROR, "空间创建者不能退出空间");
        }
        SpaceUser spaceUser = new SpaceUser();
        spaceUser.setSpaceId(spaceId);
        spaceUser.setUserId(StpUtil.getLoginIdAsLong());
        QueryWrapper<SpaceUser> queryWrapper = this.getQueryWrapper(spaceUser);
        return this.remove(queryWrapper);
    }

    /**
     * 查看空间成员列表
     */
    @Override
    public Page<SpaceUserVO> listSpaceUsers(SpaceUserQueryDTO spaceUserQueryDTO) {
        ThrowUtils.throwIf(ObjUtil.isEmpty(spaceUserQueryDTO.getSpaceId()), ResultEnum.PARAMS_ERROR);
        SpaceUser spaceUser = new SpaceUser();
        BeanUtils.copyProperties(spaceUserQueryDTO, spaceUser);

        QueryWrapper<SpaceUser> queryWrapper = this.getQueryWrapper(spaceUser);
        Page<SpaceUser> page = this.page(new Page<>(spaceUserQueryDTO.getCurrent(), spaceUserQueryDTO.getPageSize()), queryWrapper);
        List<SpaceUser> spaceUserList = page.getRecords();
        if (CollUtil.isEmpty(spaceUserList)) {
            return new Page<>();
        }
        List<SpaceUserVO> spaceUserVOList = this.getSpaceUserVOList(spaceUserList);

        //填充用户名称、头像
        Set<Long> userIdSet = spaceUserList.stream().map(SpaceUser::getUserId).collect(Collectors.toSet());
        List<User> userList = userService.listByIds(userIdSet);
        Map<Long, String> userIdNameMap = userList.stream().collect(Collectors.toMap(User::getId, User::getNickname));
        Map<Long, String> userIdAvatarMap
                = userList.stream()
                .collect(Collectors.toMap(
                        User::getId,
                        user -> user.getAvatar() == null ? "null" : user.getAvatar()
                ));

        spaceUserVOList.forEach(spaceUserVO -> {
            spaceUserVO.setUserName(userIdNameMap.get(spaceUserVO.getUserId()));
            spaceUserVO.setUserAvatar(userIdAvatarMap.get(spaceUserVO.getUserId()));
        });
        //过滤按名字搜索
        spaceUserVOList = spaceUserVOList.stream().filter(spaceUserVO -> {
            if (StrUtil.isNotBlank(spaceUserQueryDTO.getUsername())) {
                return StrUtil.containsIgnoreCase(spaceUserVO.getUserName(), spaceUserQueryDTO.getUsername());
            }
            return true;
        }).collect(Collectors.toList());

        Page<SpaceUserVO> spaceUserVOPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        spaceUserVOPage.setRecords(spaceUserVOList);
        return spaceUserVOPage;
    }

    /**
     * 修改空间成员权限
     *
     * @param spaceUserEditDTO 参数
     * @return 修改是否成功
     */
    @Override
    public boolean editUserRole(SpaceUserEditDTO spaceUserEditDTO) {
        Long userId = spaceUserEditDTO.getUserId();
        Long spaceId = spaceUserEditDTO.getSpaceId();
        Integer changeToRole = spaceUserEditDTO.getSpaceRole();

        //todo 角色选择范围待校验
        //todo 只有创建者和管理员能修改权限，且不能修改为创建者

        Space space = spaceService.getById(spaceId);
        User user = userService.getById(userId);
        ThrowUtils.throwIf(space == null || user == null, ResultEnum.NOT_FOUND_ERROR, "不存在该空间或该用户");
        SpaceUser su = new SpaceUser();
        su.setSpaceId(spaceId);
        su.setUserId(userId);
        QueryWrapper<SpaceUser> queryWrapper = this.getQueryWrapper(su);
        SpaceUser result = this.getOne(queryWrapper);
        ThrowUtils.throwIf(result == null, ResultEnum.NOT_FOUND_ERROR, "该空间无该成员记录");
        return lambdaUpdate()
                .set(SpaceUser::getSpaceRole, changeToRole)
                .eq(SpaceUser::getUserId, userId)
                .eq(SpaceUser::getSpaceId, spaceId)
                .update();
    }


    @Override
    public QueryWrapper<SpaceUser> getQueryWrapper(SpaceUser spaceUser) {
        QueryWrapper<SpaceUser> queryWrapper = new QueryWrapper<>();
        if (spaceUser == null) {
            return queryWrapper;
        }
        // 从对象中取值
        Long id = spaceUser.getId();
        Long spaceId = spaceUser.getSpaceId();
        Long userId = spaceUser.getUserId();
        Integer spaceRole = spaceUser.getSpaceRole();
        queryWrapper.eq(ObjUtil.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjUtil.isNotEmpty(spaceId), "space_id", spaceId);
        queryWrapper.eq(ObjUtil.isNotEmpty(userId), "user_id", userId);
        queryWrapper.eq(ObjUtil.isNotEmpty(spaceRole), "space_role", spaceRole);
        queryWrapper.orderBy(true, false, "space_role"); // 按角色降序排列，creator/admin/editor/viewer
        return queryWrapper;
    }


    /**
     * 脱敏获取空间、用户关系
     *
     * @param spaceUserList 参数集合（含空间id、用户id）
     * @return 脱敏后的 空间-用户关系集合
     */
    @Override
    public List<SpaceUserVO> getSpaceUserVOList(List<SpaceUser> spaceUserList) {
        // 判断输入列表是否为空
        if (CollUtil.isEmpty(spaceUserList)) {
            return Collections.emptyList();
        }
        // 对象列表 => 封装对象列表

        return spaceUserList.stream().map(SpaceUserVO::objToVo).collect(Collectors.toList());
    }

    @Override
    public SpaceUser getUserRoleInSpace(long loginIdAsLong, Long spaceId) {
        SpaceUser spaceUserInfo = this.lambdaQuery().eq(SpaceUser::getUserId, loginIdAsLong)
                .eq(SpaceUser::getSpaceId, spaceId)
                .one();
        return spaceUserInfo;
    }


}




