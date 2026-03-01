package com.jstart.qypicture.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.secure.SaSecureUtil;
import cn.dev33.satoken.stp.StpUtil;
import com.jstart.qypicture.auth.SystemRoleEnum;
import com.jstart.qypicture.constant.UserConstant;
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
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
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

    // 退出登录
    @GetMapping("/logout")
    public Result<String> logout() {
        long loginIdAsLong = StpUtil.getLoginIdAsLong();
        // 更新最后登录时间
        User user = userService.getById(loginIdAsLong);
        if (user != null) {
            User updateUser = new User();
            updateUser.setId(loginIdAsLong);
            updateUser.setLastLoginTime(new Date());
            userService.updateById(updateUser);
        }
        
        StpUtil.logout();
        return Result.success("注销成功");
    }

    // 根据ID获取用户
    @GetMapping("/{id}")
    public Result<UserInfoVO> getById(@PathVariable Long id) {
        User user = userService.getById(id);
        ThrowUtils.throwIf(user == null, ResultEnum.NOT_FOUND_ERROR, "用户不存在");
        UserInfoVO userInfoVO = userService.getUserInfoVO(user);

        // 用户的隐藏邮箱和电话
        if (userInfoVO.getEmail() != null && userInfoVO.getEmail().length() > 4) {
            String email = userInfoVO.getEmail();
            String hiddenEmail = email.charAt(0) + "****" + email.substring(email.indexOf("@") - 1);
            userInfoVO.setEmail(hiddenEmail);
        }
        if (userInfoVO.getPhone() != null && userInfoVO.getPhone().length() > 4) {
            String phone = userInfoVO.getPhone();
            String hiddenPhone = phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
            userInfoVO.setPhone(hiddenPhone);
        }

        return Result.success(userInfoVO);
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


    /**
     * 贡献排行榜
     * @return
     */
    @GetMapping("/getHotUser")
    public Result<List<UserInfoVO>> getHotUser(){

        List<UserInfoVO> hotUser = userService.getHotUser();

        return Result.success(hotUser);

    }

    /**
     * 更换用户头像
     * @param file 头像文件
     * @return 新头像URL
     */
    @PostMapping("/avatar")
    public Result<String> updateAvatar(@RequestPart("file") MultipartFile file) {
        String newAvatarUrl = userService.updateAvatar(file);
        return Result.success(newAvatarUrl);
    }

    /**
     * 发送修改密码验证码
     * @param type 验证方式：email-邮箱, phone-手机号
     * @return
     */
    @PostMapping("/password/code")
    public Result<String> sendPasswordCode(@RequestParam String type) {
        userService.sendPasswordCode(type);
        return Result.success("验证码发送成功");
    }

    /**
     * 确认修改密码
     * @param changePasswordDTO 修改密码请求参数
     * @return
     */
    @PostMapping("/password")
    public Result<String> changePassword(@RequestBody ChangePasswordDTO changePasswordDTO) {
        userService.changePassword(changePasswordDTO);
        return Result.success("密码修改成功");
    }



    //...管理端接口=============================================================


    //统计用户
    @GetMapping("/count")
    @SaCheckRole("admin")
    public Result<Long> count() {
        long count = userService.count();
        return Result.success(count);
    }

    // 账号注销
    @DeleteMapping("/{id}")
    @SaCheckRole("admin")
    public boolean delete(@PathVariable Long id) {
        User loginUser = userService.getById(id);
        ThrowUtils.throwIf(loginUser == null, ResultEnum.NOT_FOUND_ERROR, "用户不存在");
        // 考虑Boss角色的权限，Boss角色不能注销账号
        User user = userService.getById(id);
        if (user.getRole().equals(SystemRoleEnum.BOSS.getKey())){
            throw new BusinessException(ResultEnum.NO_AUTH_ERROR, "无权限");
        }
        if (user.getRole().equals(SystemRoleEnum.ADMIN.getKey())){
            //只有boss能删除管理员
            ThrowUtils.throwIf(!StpUtil.hasRole(SystemRoleEnum.BOSS.getValue()), ResultEnum.NO_AUTH_ERROR, "无权限");
        }

        StpUtil.logout(id);
        return userService.removeById(id);
    }

    // 分页查询
    @SaCheckRole("admin")
    @PostMapping("/page")
    public Result<Page<User>> page(@RequestBody UserQueryDTO userQueryDTO) {
        ThrowUtils.throwIf(userQueryDTO == null, ResultEnum.PARAMS_ERROR, "参数不能为空");
        Integer current = userQueryDTO.getCurrent();
        Integer pageSize = userQueryDTO.getPageSize();

        User user = new User();
        BeanUtils.copyProperties(userQueryDTO, user);
        if (!userQueryDTO.getIsdelete()){
            user.setDeleteTime(new Date());//未删除，随便给一个时间
        }
        QueryWrapper<User> queryWrapper = userService.getQueryWrapper(user);
        Page<User> pageResult = userService.page(new Page<>(current, pageSize), queryWrapper);
        Page<UserInfoVO> pageVoResult = new Page<>(current, pageSize, pageResult.getTotal());
        pageVoResult.setRecords(userService.getUserVOList(pageResult.getRecords()));
        return Result.success(pageResult);
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
    @SaCheckRole("boss")
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
            newUser.setPassword(SaSecureUtil.sha256(userLoginByPasswordDTO.getPassword()));
            newUser.setAvatar(UserConstant.USER_DEFAULT_AVATAR);
            userService.save(newUser);
        } catch (Exception e) {
            log.error("创建用户失败：{}", e.getMessage(), e);
            throw new BusinessException(ResultEnum.SYSTEM_ERROR, "创建失败，账号可能已存在");
        }
        return Result.success("创建成功");
    }

    /**
     * 修改用户状态(禁用/启用)
     * @param userId 用户ID
     * @param status 状态(0-禁用,1-启用)
     * @return
     */
    @PostMapping("/changeStatus")
    @SaCheckRole("boss")
    public Result<String> changeStatus(@RequestParam Long userId, @RequestParam Integer status) {
        ThrowUtils.throwIf(userId == null || status == null, ResultEnum.PARAMS_ERROR, "参数不能为空");
        ThrowUtils.throwIf(status != 0 && status != 1, ResultEnum.PARAMS_ERROR, "状态参数错误");
            
        User user = userService.getById(userId);
        ThrowUtils.throwIf(user == null, ResultEnum.NOT_FOUND_ERROR, "用户不存在");
            
        if (user.getRole().equals(SystemRoleEnum.BOSS.getKey())) {
            throw new BusinessException(ResultEnum.PARAMS_ERROR, "不能修改老板状态");
        }
            
        User updateUser = new User();
        updateUser.setId(userId);
        updateUser.setStatus(status);
        boolean result = userService.updateById(updateUser);
            
        return result ? Result.success("修改成功") : Result.error(ResultEnum.SYSTEM_ERROR, "修改失败");
    }

}
