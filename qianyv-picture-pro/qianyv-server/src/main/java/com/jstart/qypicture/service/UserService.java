package com.jstart.qypicture.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jstart.qypicture.model.dto.UserLoginByCodeDTO;
import com.jstart.qypicture.model.dto.UserLoginByPasswordDTO;
import com.jstart.qypicture.model.dto.SendCodeDTO;
import com.jstart.qypicture.model.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jstart.qypicture.model.vo.UserVO;

import java.util.List;

/**
 * @author 28435
 * @description 针对表【user(用户表)】的数据库操作Service
 * @createDate 2025-10-01 08:08:47
 */
public interface UserService extends IService<User> {

    /**
     * 发送验证码（邮箱或短信）
     */
    void sendCode(SendCodeDTO sendCodeDTO);

    /**
     * 登录注册
     */
    void loginOrRegister(UserLoginByCodeDTO userLoginByCodeDTO);

    /**
     * 密码登录
     */
    void loginWithPassword(UserLoginByPasswordDTO userLoginByPasswordDTO);

    public QueryWrapper<User> getQueryWrapper(User user);

    UserVO getUserVO(User user);

    List<UserVO> getUserVOList(List<User> userList);
}
