package com.jstart.qypicture.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.util.Date;

import lombok.Data;

/**
 * 用户表
 *
 * @TableName user
 */
@TableName(value = "user")
@Data
public class User {
    /**
     * 主键id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 密码
     */
    private String password;

    /**
     * 用户昵称（最长10汉字）
     */
    private String nickname;

    /**
     * 头像URL
     */
    private String avatar;

    /**
     * 角色类型：0-Boss, 1-管理员, 2-普通用户
     */
    private Integer role;

    /**
     * 状态：0-禁用,1-启用,2-锁定
     */
    private Integer status;

    /**
     * 性别：0-保密,1-男,2-女
     */
    private Integer gender;

    /**
     * 标签
     */
    private String tag;

    /**
     * 用户简介
     */
    private String introduction;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 最后登录时间
     */
    private Date lastLoginTime;

    /**
     * 逻辑删除：null-正常, 非null-删除时间
     */
    private Date deleteTime;
}