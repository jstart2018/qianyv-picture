package com.jstart.qypicture.auth;

import cn.dev33.satoken.stp.StpInterface;
import com.jstart.qypicture.enums.ResultEnum;
import com.jstart.qypicture.exception.BusinessException;
import com.jstart.qypicture.model.entity.User;
import com.jstart.qypicture.service.UserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * 自定义权限加载接口实现类
 */
@Component    // 保证此类被 SpringBoot 扫描，完成 Sa-Token 的自定义权限验证扩展
@Slf4j
public class StpInterfaceImpl implements StpInterface {

    @Resource
    private UserService userService;
    /**
     * 返回一个账号所拥有的权限码集合 
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        return Collections.emptyList();
    }

    /**
     * 返回一个账号所拥有的角色标识集合 (权限与角色可分开校验)
     */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        User user = userService.getById(Long.valueOf(loginId.toString()));
        SystemRoleEnum systemRoleEnum = SystemRoleEnum.getByKey(user.getRole());
        if (systemRoleEnum == null) {
            log.error("sa-token 获取角色失败，用户id：{}，不能存在对应角色",loginId);
            throw new BusinessException(ResultEnum.SYSTEM_ERROR,"系统异常，请联系管理员");
        }
        if (systemRoleEnum.equals(SystemRoleEnum.BOSS)) {
            return List.of(SystemRoleEnum.user.getValue(), SystemRoleEnum.ADMIN.getValue(), SystemRoleEnum.BOSS.getValue());
        }else if (systemRoleEnum.equals(SystemRoleEnum.ADMIN)){
            return List.of(SystemRoleEnum.user.getValue(), SystemRoleEnum.ADMIN.getValue());
        }else {
            return List.of(SystemRoleEnum.user.getValue());
        }
    }




}
