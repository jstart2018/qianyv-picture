package com.qypicture.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 用户表
 * @TableName user
 */
@TableName(value ="user")
@Data
public class User {
    /**
     * 用户唯一标识
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 登录账号
     */
    private String userAccount;

    /**
     * 密码
     */
    private String userPassword;

    /**
     * 微信openId（支持登录）
     */
    private String openId;

    /**
     * 用户邮箱（支持登录）
     */
    private String email;

    /**
     * 用户昵称（显示用）
     */
    private String userName;

    /**
     * 头像URL（支持CDN路径）
     */
    private String userAvatar;

    /**
     * 用户简介（个人空间/博客展示）
     */
    private String userProfile;

    /**
     * 系统角色：0-普通用户，1-管理员，2-超级管理员
     */
    private Integer userRole;

    /**
     * 性别：0-未知，1-男，2-女
     */
    private Integer gender;

    /**
     * 账号创建时间
     */
    private LocalDateTime createTime;

    /**
     * 最后修改时间
     */
    private LocalDateTime editTime;

    /**
     * 更改时间（数据库自动更新）
     */
    private LocalDateTime updateTime;

    /**
     * 逻辑删除时间（null表示未删除）
     */
    private LocalDateTime deleteTime;
}