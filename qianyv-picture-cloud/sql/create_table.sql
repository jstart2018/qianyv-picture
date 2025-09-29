-- 创建库
create database if not exists qypicture;

use qypicture;
-- 用户表
create table if not exists user
(
    id           bigint auto_increment comment '用户唯一标识' primary key,
    user_account  varchar(128)                           null comment '登录账号',
    user_password varchar(128)                           null comment '密码',
    open_id       varchar(128)                           null comment '微信openId（支持登录）',
    email         varchar(128)                           null comment '用户邮箱（支持登录）',
    user_name     varchar(64)                            null comment '用户昵称（显示用）',
    user_avatar   varchar(1024)                          null comment '头像URL（支持CDN路径）',
    user_profile  text                                   null comment '用户简介（个人空间/博客展示）',
    user_role     tinyint default 0            not null comment '系统角色：0-普通用户，1-管理员，2-超级管理员',
    gender        tinyint                          null comment '性别：0-未知，1-男，2-女',
    create_time   datetime     default CURRENT_TIMESTAMP not null comment '账号创建时间',
    edit_time     datetime     default CURRENT_TIMESTAMP not null comment '最后修改时间',
    update_time   datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更改时间（数据库自动更新）',
    delete_time   datetime      default null              comment '逻辑删除时间（null表示未删除）',
    -- 唯一约束：确保账号和邮箱不重复
    UNIQUE KEY uk_userAccount (user_account),
    UNIQUE KEY uk_openId (open_id),
    UNIQUE KEY uk_email (email),
    -- 索引：优化查询性能
    INDEX idx_userName (user_name),
    INDEX idx_deleteTime (delete_time)
) comment '用户表' collate = utf8mb4_unicode_ci;
