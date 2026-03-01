package com.jstart.qypicture.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jstart.qypicture.model.dto.ChangePasswordDTO;
import com.jstart.qypicture.model.dto.UserLoginByCodeDTO;
import com.jstart.qypicture.model.dto.UserLoginByPasswordDTO;
import com.jstart.qypicture.model.dto.SendCodeDTO;
import com.jstart.qypicture.model.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jstart.qypicture.model.vo.UserInfoVO;
import org.springframework.web.multipart.MultipartFile;

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

    UserInfoVO getUserInfoVO(User user);

    List<UserInfoVO> getUserVOList(List<User> userList);

    /**
     * 关注/取关 切换
     * @param userId
     */
    void followToggle(Long userId);

    /**
     * 查看是否关注了该用户
     * @param userId
     * @return
     */
    Boolean checkFollow(Long userId);

    /**
     * 获取热门用户
     * @return
     */
    List<UserInfoVO> getHotUser();

    /**
     * 更换用户头像
     * @param file 头像文件
     * @return 新头像URL
     */
    String updateAvatar(MultipartFile file);

    /**
     * 发送修改密码验证码
     * @param type 验证方式：email-邮箱, phone-手机号
     */
    void sendPasswordCode(String type);

    /**
     * 修改密码
     * @param changePasswordDTO 修改密码请求参数
     */
    void changePassword(ChangePasswordDTO changePasswordDTO);
}
