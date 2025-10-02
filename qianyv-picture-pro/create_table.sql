create database if not exists qypicture;
use qypicture;

-- 1、用户表
drop table if exists `user`;
CREATE TABLE `user`
(
    `id`              bigint       NOT NULL AUTO_INCREMENT COMMENT '主键id',
    `email`           varchar(100) COMMENT '邮箱',
    `phone`           varchar(20) COMMENT '手机号',
    `password`        varchar(100) NOT NULL COMMENT '密码',
    `nickname`        varchar(30)           DEFAULT NULL COMMENT '用户昵称（最长10汉字）',
    `avatar`          varchar(255)          DEFAULT NULL COMMENT '头像URL',
    `role`            tinyint      NOT NULL DEFAULT 2 COMMENT '角色类型：0-Boss, 1-管理员, 2-普通用户',
    `status`          tinyint      NOT NULL DEFAULT 1 COMMENT '状态：0-禁用,1-启用,2-锁定',
    `gender`          tinyint      NOT NULL DEFAULT 0 COMMENT '性别：0-保密,1-男,2-女',
    `tag`             varchar(30)           DEFAULT NULL COMMENT '标签',
    `introduction`    varchar(1024)         DEFAULT '事了拂衣去，深藏功与名~' COMMENT '用户简介',
    `create_time`     datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     datetime              DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `last_login_time` datetime              DEFAULT NULL COMMENT '最后登录时间',
    `delete_time`     datetime              DEFAULT null COMMENT '逻辑删除：null-正常, 非null-删除时间',
    -- 强制email和phone至少有一个不为空
    CONSTRAINT `chk_contact_info` CHECK (
        (`email` IS NOT NULL) OR
        (`phone` IS NOT NULL)
        ),
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_email` (`email`),
    UNIQUE KEY `uk_phone` (`phone`),
    KEY `idx_nickname_role_status` (`nickname`, `role`, `status`), -- 联合索引加速权限查询
    KEY `idx_delete_create_lastlogin` (`delete_time`, `create_time`, `last_login_time`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='用户表';


