package com.jstart.qypicture.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jstart.qypicture.constant.UserConstant;
import com.jstart.qypicture.enums.ResultEnum;
import com.jstart.qypicture.exception.BusinessException;
import com.jstart.qypicture.manager.sendCodeTemplate.SendCodeTemplate;
import com.jstart.qypicture.manager.sendCodeTemplate.SendEmail;
import com.jstart.qypicture.manager.sendCodeTemplate.SendSMS;
import com.jstart.qypicture.model.dto.UserLoginByCodeDTO;
import com.jstart.qypicture.model.dto.UserLoginByPasswordDTO;
import com.jstart.qypicture.model.dto.SendCodeDTO;
import com.jstart.qypicture.model.entity.User;
import com.jstart.qypicture.model.vo.UserVO;
import com.jstart.qypicture.service.UserService;
import com.jstart.qypicture.mapper.UserMapper;
import com.jstart.qypicture.utils.ThrowUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author 28435
 * @description 针对表【user(用户表)】的数据库操作Service实现
 * @createDate 2025-10-01 08:08:47
 */
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {
    @Resource
    private SendEmail sendEmail;

    @Resource
    private SendSMS sendSMS;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;


    @Override
    public void sendCode(SendCodeDTO sendCodeDTO) {
        SendCodeTemplate sendCodeTemplate;

        String account = sendCodeDTO.getAccount();
        //1. 判断账号类型(邮箱或手机号)
        if (account.contains("@")) {
            sendCodeTemplate = sendEmail;
        } else {
            sendCodeTemplate = sendSMS;
        }
        //2. 发送验证码
        sendCodeTemplate.sendCode(account);
    }

    @Override
    public void loginOrRegister(UserLoginByCodeDTO userLoginByCodeDTO) {
        //1. 获取验证码
        Object codeFromRedis = redisTemplate.opsForValue()
                .get(UserConstant.USER_LOGIN_CODE_KEY + userLoginByCodeDTO.getAccount());

        // 2. 校验验证码
        if (codeFromRedis == null) {
            throw new BusinessException(ResultEnum.NOT_FOUND_ERROR,"验证码已过期");
        }
        String codeStr = String.valueOf(codeFromRedis);
        if (StringUtils.isBlank(codeStr) || !codeStr.equals(userLoginByCodeDTO.getCode())) {
            throw new BusinessException(ResultEnum.NOT_FOUND_ERROR,"验证码错误");
        }

        //3. 查询用户是否存在
        User user = new User();
        if (userLoginByCodeDTO.getAccount().contains("@")) {
            user.setEmail(userLoginByCodeDTO.getAccount());
        } else {
            user.setPhone(userLoginByCodeDTO.getAccount());
        }
        //4. 不存在则注册
        if(this.count(getQueryWrapper(user)) == 0){
            user.setNickname("用户" + ((int) ((Math.random() * 9 + 1) * 100000)));
            try {
                this.saveOrUpdate(user);
            }catch (Exception e){
                ThrowUtils.throwIf(true, ResultEnum.SYSTEM_ERROR, "注册失败，请稍后重试");
            }
        }
        user = this.getOne(getQueryWrapper(user));
        //5. 存在则登录
        StpUtil.login(user.getId());

        //6. 删除验证码
        redisTemplate.delete(UserConstant.USER_LOGIN_CODE_KEY + userLoginByCodeDTO.getAccount());
        log.info("用户登录成功, 用户id: {}", user.getId());

    }

    @Override
    public void loginWithPassword(UserLoginByPasswordDTO userLoginByPasswordDTO) {
        // 查询用户是否存在
        User user = new User();
        if (userLoginByPasswordDTO.getAccount().contains("@")) {
            user.setEmail(userLoginByPasswordDTO.getAccount());
        } else {
            user.setPhone(userLoginByPasswordDTO.getAccount());
        }
        user = this.getOne(getQueryWrapper(user));
        ThrowUtils.throwIf(user == null,ResultEnum.NOT_FOUND_ERROR,"账号或密码错误");
        //存在就登录
        StpUtil.login(user.getId());
    }

    @Override
    public QueryWrapper<User> getQueryWrapper(User user) {
        Long id = user.getId();
        String email = user.getEmail();
        String phone = user.getPhone();
        String nickname = user.getNickname();
        Integer role = user.getRole();
        Integer status = user.getStatus();
        Integer gender = user.getGender();
        String tag = user.getTag();
        String introduction = user.getIntroduction();

        QueryWrapper<User> qw = new QueryWrapper<>();
        qw.eq(id != null, "id", id);
        qw.eq(!StringUtils.isBlank(email), "email", email);
        qw.eq(!StringUtils.isBlank(phone), "phone", phone);
        qw.like(!StringUtils.isBlank(nickname), "nickname", nickname);
        qw.eq(role != null, "role", role);
        qw.eq(status != null, "status", status);
        qw.eq(gender != null, "gender",gender);
        qw.like(!StringUtils.isBlank(tag), "tag", tag);
        qw.like(!StringUtils.isBlank(introduction), "introduction", introduction);
        return qw;
    }

    @Override
    public UserVO getUserVO(User user) {
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user,userVO);
        return userVO;
    }

    @Override
    public List<UserVO> getUserVOList(List<User> userList) {
        return userList.stream().map(this::getUserVO).toList();
    }



}
