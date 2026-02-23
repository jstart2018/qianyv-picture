package com.jstart.qypicture.controller;


import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jstart.qypicture.enums.ResultEnum;
import com.jstart.qypicture.exception.BusinessException;
import com.jstart.qypicture.model.dto.SpaceUserAddDTO;
import com.jstart.qypicture.model.dto.SpaceUserEditDTO;
import com.jstart.qypicture.model.dto.SpaceUserQueryDTO;
import com.jstart.qypicture.model.entity.Space;
import com.jstart.qypicture.model.vo.SpaceUserVO;
import com.jstart.qypicture.model.vo.SpaceVO;
import com.jstart.qypicture.result.Result;
import com.jstart.qypicture.service.SpaceService;
import com.jstart.qypicture.service.SpaceUserService;
import com.jstart.qypicture.utils.ThrowUtils;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;

@RestController
@RequestMapping("/space/")
public class SpaceController {

    @Resource
    private SpaceService spaceService;

    @Resource
    private SpaceUserService spaceUserService;

    /**
     * 检查用户是否有空间
     *
     * @param userId 用户id
     * @return 空间id
     */
    @GetMapping("checkHas")
    public Result<Long> checkHasSpace(@RequestParam("userId") Long userId) {
        if (userId == null) {
            return Result.error(ResultEnum.PARAMS_ERROR, "用户错误");
        }
        Space space = spaceService.lambdaQuery().eq(Space::getUserId, userId).one();
        if (space == null) {
            return Result.success(-1L);
        }
        return Result.success(space.getId());

    }



    /**
     * 创建空间
     *
     * @return spaceId
     */
    @GetMapping("add")
    public Result<Long> addSpace() {

        long spaceId = spaceService.createSpace();
        if (spaceId <= 0) {
            return Result.error(ResultEnum.SYSTEM_ERROR, "创建空间失败");
        }
        return Result.success(spaceId);
    }

    /**
     * 新建目录
     *
     * @return spaceId
     */
    @GetMapping("addCategory")
    public Result<Long> addCategory(String categoryName) {
        return null;
    }


    /**
     * 更改空间
     */
    @PostMapping("edit")
    public Result<Boolean> editSpace(@RequestParam("spaceId") Long spaceId,
                                     @RequestParam("name") String name) {
        boolean result = spaceService.editSpace(spaceId, name);
        if (!result) {
            return Result.error(ResultEnum.SYSTEM_ERROR, "更改名称失败");
        }
        return Result.success(true);
    }

    /**
     * 空间升级
     */
    @PostMapping("upgrade")
    public Result<Boolean> upgradeSpace(@RequestParam("spaceId") Long spaceId,
                                        @RequestParam("level") Integer level) {
        boolean result = spaceService.upgradeSpace(spaceId, level);
        if (!result) {
            return Result.error(ResultEnum.SYSTEM_ERROR, "升级空间失败");
        }
        return Result.success(true);
    }

    /**
     * 获取空间信息
     *
     * @param spaceId 空间id
     * @return 空间信息
     */
    @GetMapping("getSpaceInfo")
    public Result<SpaceVO> getSpaceInfo(@RequestParam("spaceId") Long spaceId) {

        SpaceVO spaceVO = spaceService.getUserSpaceInfoList(spaceId, null, null).getFirst();

        return Result.success(spaceVO);

    }

    /**
     * 查看用户的所有空间，可查看只拥有某个角色的空间
     *
     * @param spaceRole 查看拥有该角色的空间
     * @return 空间信息
     */
    @GetMapping("getUserSpaceList")
    public Result<List<SpaceVO>> getUserSpaceList(@RequestParam(value = "spaceRole") HashSet<Integer> spaceRole) {

        List<SpaceVO> spaceInfoList = null;
        try {
            spaceInfoList = spaceService.getUserSpaceInfoList(null, StpUtil.getLoginIdAsLong(), spaceRole);
        } catch (Exception e) {
            throw new BusinessException(ResultEnum.NO_AUTH_ERROR);
        }

        return Result.success(spaceInfoList);

    }

    /**
     * todo 邀请成员controller
     */
    @PostMapping("inviteMember")
    public Result<Boolean> inviteMember(@RequestParam("spaceId") Long spaceId,
                                        @RequestParam("userId") Long userId) {
        SpaceUserAddDTO spaceUserAddDTO = new SpaceUserAddDTO();
        spaceUserAddDTO.setSpaceId(spaceId);
        spaceUserAddDTO.setUserId(userId);
        long result = spaceUserService.addSpaceUser(spaceUserAddDTO);
        if (result <= 0) {
            return Result.error(ResultEnum.SYSTEM_ERROR, "邀请成员失败");
        }
        return Result.success(true);
    }

    /**
     * 踢出成员
     */
    @PostMapping("kickout")
    public Result<Boolean> kickOutMember(@RequestParam("spaceId") Long spaceId,
                                         @RequestParam("userId") Long userId) {
        boolean result = spaceUserService.kickOutMember(spaceId, userId);
        if (!result) {
            return Result.error(ResultEnum.SYSTEM_ERROR, "踢出成员失败");
        }
        return Result.success(true);
    }

    /**
     * 获取空间内成员列表
     */
    @PostMapping("/user/list")
    public Result<Page<SpaceUserVO>> listSpaceUsers(@RequestBody SpaceUserQueryDTO spaceUserQueryDTO) {
        ThrowUtils.throwIf(spaceUserQueryDTO == null || spaceUserQueryDTO.getSpaceId() == null, ResultEnum.PARAMS_ERROR);

        Page<SpaceUserVO> spaceUserVOPage = spaceUserService.listSpaceUsers(spaceUserQueryDTO);

        return Result.success(spaceUserVOPage);
    }

    /**
     * 修改成员角色
     */
    @PostMapping("editRole")
    public Result<Boolean> editUserRole(@RequestBody SpaceUserEditDTO spaceUserEditDTO) {
        boolean result = spaceUserService.editUserRole(spaceUserEditDTO);
        if (!result) {
            return Result.error(ResultEnum.SYSTEM_ERROR, "修改成员角色失败");
        }
        return Result.success(true);
    }


}
