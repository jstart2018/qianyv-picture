package com.jstart.qypicture.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.stp.StpUtil;
import com.jstart.qypicture.auth.SystemRoleEnum;
import com.jstart.qypicture.enums.ResultEnum;
import com.jstart.qypicture.exception.BusinessException;
import com.jstart.qypicture.model.dto.*;
import com.jstart.qypicture.model.vo.UserInfoVO;
import com.jstart.qypicture.result.Result;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jstart.qypicture.service.FollowService;
import com.jstart.qypicture.service.UserService;
import com.jstart.qypicture.model.entity.User;
import com.jstart.qypicture.utils.ThrowUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    @Resource
    private UserService userService;
    @Resource
    private FollowService followService;


    // 发送验证码
    @PostMapping("/code")
    public Result<String> sendCode(@RequestBody SendCodeDTO sendCodeDTO) {
        ThrowUtils.throwIf(sendCodeDTO == null,
                ResultEnum.PARAMS_ERROR, "参数不能为空");
        userService.sendCode(sendCodeDTO);
        return Result.success("验证码发送成功");
    }

    // 验证码登录
    @PostMapping("/login/code")
    public Result<String> loginWithCode(@RequestBody UserLoginByCodeDTO userLoginByCodeDTO) {
        ThrowUtils.throwIf(userLoginByCodeDTO == null || userLoginByCodeDTO.getEmailOrPhone() == null || userLoginByCodeDTO.getCode() == null,
                ResultEnum.PARAMS_ERROR, "参数不能为空");

        userService.loginOrRegister(userLoginByCodeDTO);

        return Result.success("登录成功");
    }

    // 密码登录
    @PostMapping("/login/password")
    public Result<String> loginWithPassword(@RequestBody UserLoginByPasswordDTO userLoginByPasswordDTO) {
        ThrowUtils.throwIf(userLoginByPasswordDTO == null || userLoginByPasswordDTO.getAccount() == null
                , ResultEnum.PARAMS_ERROR, "参数不能为空");
        ThrowUtils.throwIf(userLoginByPasswordDTO.getPassword() == null, ResultEnum.PARAMS_ERROR, "密码不能为空且不能少于6位");
        userService.loginWithPassword(userLoginByPasswordDTO);
        return Result.success("登录成功");
    }

    // 获取当前登录用户
    @GetMapping("/loginUser")
    public Result<UserInfoVO> getLoginUser() {
        Long loginId = StpUtil.getLoginIdAsLong();
        User user = userService.getById(loginId);
        ThrowUtils.throwIf(user == null, ResultEnum.NOT_FOUND_ERROR, "用户不存在");
        return Result.success(userService.getUserInfoVO(user));
    }

    // 注销
    @GetMapping("/logout")
    public Result<String> logout() {
        StpUtil.getLoginId();
        StpUtil.logout();
        return Result.success("注销成功");
    }

    // 根据ID获取用户
    @GetMapping("/{id}")
    public Result<UserInfoVO> getById(@PathVariable Long id) {
        User user = userService.getById(id);
        ThrowUtils.throwIf(user == null, ResultEnum.NOT_FOUND_ERROR, "用户不存在");
        return Result.success(userService.getUserInfoVO(user));
    }

    // 用户编辑
    @PutMapping("/{id}")
    public boolean update(@RequestBody UserEditDTO userEditDTO) {
        long loginId = StpUtil.getLoginIdAsLong();
        User loginUser = new User();
        BeanUtils.copyProperties(userEditDTO, loginUser);
        loginUser.setId(loginId);
        return userService.updateById(loginUser);
    }

    // 账号注销
    @DeleteMapping("/{id}")
    public boolean delete(@PathVariable Long id) {
        User loginUser = userService.getById(id);
        ThrowUtils.throwIf(loginUser == null, ResultEnum.NOT_FOUND_ERROR, "用户不存在");
        //仅管理员和本人能注销账号
        if (!id.equals(StpUtil.getLoginIdAsLong()) && !StpUtil.hasRole("admin")) {
            throw new BusinessException(ResultEnum.NO_AUTH_ERROR, "无权限");
        }
        StpUtil.logout();
        return userService.removeById(id);
    }

    // 分页查询
    @SaCheckRole("admin")
    @PostMapping("/page")
    public Page<User> page(@RequestBody UserQueryDTO userQueryDTO) {
        ThrowUtils.throwIf(userQueryDTO == null, ResultEnum.PARAMS_ERROR, "参数不能为空");
        Integer current = userQueryDTO.getCurrent();
        Integer pageSize = userQueryDTO.getPageSize();

        User user = new User();
        BeanUtils.copyProperties(userQueryDTO, user);
        QueryWrapper<User> queryWrapper = userService.getQueryWrapper(user);
        Page<User> pageResult = userService.page(new Page<>(current, pageSize), queryWrapper);
        Page<UserInfoVO> pageVoResult = new Page<>(current, pageSize, pageResult.getTotal());
        pageVoResult.setRecords(userService.getUserVOList(pageResult.getRecords()));
        return pageResult;
    }

    @GetMapping("changeRole")
    @SaCheckRole("boss")
    public Result<String> changeRole(Long userId) {
        ThrowUtils.throwIf(userId == null, ResultEnum.PARAMS_ERROR, "参数不能为空");
        User user = userService.getById(userId);
        ThrowUtils.throwIf(user == null, ResultEnum.NOT_FOUND_ERROR, "用户不存在");
        User updateUser = new User();
        updateUser.setId(userId);
        if (user.getRole().equals(SystemRoleEnum.BOSS.getKey())) {
            throw new BusinessException(ResultEnum.PARAMS_ERROR, "不能修改老板角色");
        } else if (user.getRole().equals(SystemRoleEnum.ADMIN.getKey())) {
            updateUser.setRole(SystemRoleEnum.user.getKey());
        } else {
            updateUser.setRole(SystemRoleEnum.ADMIN.getKey());
        }
        boolean result = userService.updateById(updateUser);
        return result ? Result.success("修改成功") : Result.error(ResultEnum.SYSTEM_ERROR, "修改失败");
    }

    @PostMapping("/createUser")
    public Result<String> createUser(@RequestBody UserLoginByPasswordDTO userLoginByPasswordDTO) {
        ThrowUtils.throwIf(userLoginByPasswordDTO == null || userLoginByPasswordDTO.getAccount() == null
                || userLoginByPasswordDTO.getPassword() == null, ResultEnum.PARAMS_ERROR, "参数不能为空");
        try {
            User newUser = new User();
            if (userLoginByPasswordDTO.getAccount().contains("@")) {
                newUser.setEmail(userLoginByPasswordDTO.getAccount());
            } else {
                newUser.setPhone(userLoginByPasswordDTO.getAccount());
            }
            newUser.setPassword(userLoginByPasswordDTO.getPassword());
            userService.save(newUser);
        } catch (Exception e) {
            log.error("创建用户失败：{}", e.getMessage(), e);
            throw new BusinessException(ResultEnum.SYSTEM_ERROR, "创建失败，账号可能已存在");
        }
        return Result.success("创建成功");
    }

    @PostMapping("/followToggle")
    public Result<?> followToggle(@RequestParam Long userId) {
        ThrowUtils.throwIf(userId == null, ResultEnum.PARAMS_ERROR, "参数不能为空");
        userService.followToggle(userId);
        return Result.success();
    }

    /**
     * 查看是否关注该用户
     * @param userId
     * @return
     */
    @PostMapping("/checkFollow")
    public Result<Boolean> checkFollow(@RequestParam Long userId) {
        ThrowUtils.throwIf(userId == null, ResultEnum.PARAMS_ERROR, "参数不能为空");
        Boolean result = userService.checkFollow(userId);
        return Result.success(result);
    }


    @GetMapping("/getHotUser")
    public Result<List<UserInfoVO>> getHotUser(){

        List<UserInfoVO> hotUser = userService.getHotUser();

        return Result.success(hotUser);

    }

}
