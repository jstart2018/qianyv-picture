package com.jstart.qypicture.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jstart.qypicture.enums.ResultEnum;
import com.jstart.qypicture.model.dto.SpaceUserEditDTO;
import com.jstart.qypicture.model.dto.SpaceUserQueryDTO;
import com.jstart.qypicture.model.entity.Space;
import com.jstart.qypicture.model.vo.SpaceUserVO;
import com.jstart.qypicture.result.Result;
import com.jstart.qypicture.service.SpaceService;
import com.jstart.qypicture.service.SpaceUserService;
import com.jstart.qypicture.utils.ThrowUtils;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/space/")
public class SpaceController {

    @Resource
    private SpaceService spaceService;

    @Resource
    private SpaceUserService spaceUserService;

    /**
     * 创建空间
     * @return spaceId
     */
    @GetMapping("create")
    public Result<Long> createSpace(){

        long spaceId = spaceService.createSpace();
        if (spaceId <= 0) {
            return Result.error(ResultEnum.SYSTEM_ERROR,"创建空间失败");
        }
        return Result.success(spaceId);
    }

    /**
     * 更改空间
     */
    @PostMapping("edit")
    public Result<Boolean> editSpace(@RequestParam("spaceId") Long spaceId,
                                       @RequestParam("name") String name){
        boolean result = spaceService.editSpace(spaceId, name);
        if (!result) {
            return Result.error(ResultEnum.SYSTEM_ERROR,"升级空间失败");
        }
        return Result.success(true);
    }

    /**
     * 空间升级
     */
    @PostMapping("upgrade")
    public Result<Boolean> upgradeSpace(@RequestParam("spaceId") Long spaceId,
                                       @RequestParam("level") Integer level){
        boolean result = spaceService.upgradeSpace(spaceId, level);
        if (!result) {
            return Result.error(ResultEnum.SYSTEM_ERROR,"升级空间失败");
        }
        return Result.success(true);
    }

    /**
     * 获取空间信息
     */
    @GetMapping("get")
    public Result<?> getSpaceInfo(@RequestParam("spaceId") Long spaceId) {
        Space space = spaceService.getById(spaceId);
        if (space == null) {
            return Result.error(ResultEnum.PARAMS_ERROR,"空间不存在");
        }
        return Result.success(spaceService.getSpaceVO(space));

    }

    /**
     * todo 邀请成员controller
     */

    /**
     * 踢出成员
     */
    @PostMapping("kickout")
    public Result<Boolean> kickOutMember(@RequestParam("spaceId") Long spaceId,
                                       @RequestParam("userId") Long userId){
        boolean result = spaceUserService.kickOutMember(spaceId, userId);
        if (!result) {
            return Result.error(ResultEnum.SYSTEM_ERROR,"踢出成员失败");
        }
        return Result.success(true);
    }

    /**
     * 获取空间内成员列表
     */
    @PostMapping("list")
    public Result<Page<SpaceUserVO>> listSpaceUsers(@RequestBody SpaceUserQueryDTO spaceUserQueryDTO) {
        ThrowUtils.throwIf(spaceUserQueryDTO == null || spaceUserQueryDTO.getSpaceId() == null, ResultEnum.PARAMS_ERROR);

        Page<SpaceUserVO> spaceUserVOPage = spaceUserService.listSpaceUsers(spaceUserQueryDTO);

        return Result.success(spaceUserVOPage);
    }

    /**
     * 修改成员角色
     */
    @PostMapping("editRole")
    public Result<Boolean> editUserRole(@RequestBody SpaceUserEditDTO spaceUserEditDTO){
        boolean result = spaceUserService.editUserRole(spaceUserEditDTO);
        if (!result) {
            return Result.error(ResultEnum.SYSTEM_ERROR,"修改成员角色失败");
        }
        return Result.success(true);
    }


}
