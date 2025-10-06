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
    `nickname`        varchar(30)           DEFAULT '用户' NOT NULL COMMENT '用户昵称（最长10汉字）',
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

-- 2、公共图片表
drop table if exists pub_picture;
create table pub_picture
(
    id             bigint auto_increment comment 'id' primary key,
    user_id        bigint                             not null comment '创建用户 id',
    blog_id        bigint comment '关联博客id',
    url            varchar(512)                       not null comment '图片 url',
    introduction   varchar(512)                       null comment '简介',
    category_id    bigint                             not null comment '分类id',
    tags           json                               null comment '标签（JSON 数组）',
    pic_size       bigint                             null comment '图片体积',
    pic_width      int                                null comment '图片宽度',
    pic_height     int                                null comment '图片高度',
    pic_scale      double                             null comment '图片宽高比例',
    pic_format     varchar(32)                        null comment '图片格式',
    space_type     tinyint                            not null comment '存储类型：0-公共; 1-私有; 2-团队',
    space_id       bigint   default 0                 not null comment '所属空间id，默认0表示公共空间',
    collect_count  bigint   default 0                 not null comment '收藏数量',
    upload_status  tinyint  DEFAULT 0                 NOT NULL COMMENT '上传状态：0-temp临时; 1-formal正式',
    is_recommend   tinyint  DEFAULT 0                 NOT NULL COMMENT '是否精选：0-否; 1-是',
    review_status  tinyint  DEFAULT 0                 NOT NULL COMMENT '审核状态：0-待审核; 1-通过; 2-拒绝',
    review_message VARCHAR(512)                       NULL COMMENT '审核信息',
    reviewer_id    BIGINT                             NULL COMMENT '审核人 ID',
    review_time    datetime default NULL COMMENT '审核时间',
    create_time    datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time    datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    delete_time    datetime DEFAULT null COMMENT '逻辑删除：null-正常, 非null-删除时间',
    INDEX idx_tags ((JSON_UNQUOTE(JSON_EXTRACT(tags, '$[*]')))), -- 函数索引优化json查询(mysql 8.0+ 支持)
    INDEX idx_category (category_id),                            -- 提升基于分类的查询性能
    INDEX idx_userId (user_id),                                  -- 提升基于用户 ID 的查询性能
    INDEX idx_spaceId (space_id),                                -- 提升基于空间 ID 的查询性能
    INDEX idx_uploadStatus (upload_status),                      -- 提升基于上传状态的查询性能
    INDEX idx_reviewStatus (review_status),                      -- 提升基于审核状态的查询性能
    INDEX idx_blogId (blog_id)                                   -- 提升基于博客 ID 的查询性能
) comment '图片表' collate = utf8mb4_unicode_ci;

-- 3、图片分类表
drop table if exists pic_category;
create table pic_category
(
    id            bigint auto_increment comment '分类id' primary key,
    category_name varchar(256)                       not null comment '分类名称（如“魅力/迷人”“自然/风景”）',
    sort          int      default 0                 not null comment '排序权重（数字越大越靠前）',
    create_time   datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time   datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',

    -- 核心索引
    unique index uk_category_name (category_name),      -- 确保分类名唯一，同时加速精确查询
    index idx_category_name_prefix (category_name(64)), -- 加速前缀匹配查询（如按“自然%”筛选）
    index idx_sort (sort)                               -- 加速按排序权重的查询（如获取“排序靠前的前10个分类”）
) comment '图片分类表' collate = utf8mb4_unicode_ci;


-- 4、空间图片表
drop table if exists pri_picture;
create table pri_picture
(
    id             bigint auto_increment comment 'id' primary key,
    user_id        bigint                             not null comment '创建用户 id',
    space_id       bigint   default 0                 not null comment '所属空间id',
    catalog_id     bigint                             not null comment '目录 id',
    url            varchar(512)                       not null comment '图片 url',
    introduction   varchar(512)                       null comment '简介',
    tags           json                               null comment '标签（JSON 数组）',
    pic_size       bigint                             null comment '图片体积',
    pic_width      int                                null comment '图片宽度',
    pic_height     int                                null comment '图片高度',
    pic_scale      double                             null comment '图片宽高比例',
    pic_format     varchar(32)                        null comment '图片格式',
    upload_status  tinyint  DEFAULT 0                 NOT NULL COMMENT '上传状态：0-temp临时; 1-formal正式',
    review_status  tinyint  DEFAULT 0                 NOT NULL COMMENT '审核状态：0-待审核; 1-临时通过; 2-正式通过; 3-拒绝',
    review_message VARCHAR(512)                       NULL COMMENT '审核信息',
    reviewer_id    BIGINT                             NOT NULL DEFAULT 0 COMMENT '审核人 ID，0-表示系统自动审核',
    review_time    datetime default NULL COMMENT '审核时间',
    create_time    datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time    datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    delete_time    datetime DEFAULT null COMMENT '逻辑删除：null-正常, 非null-删除时间',
    INDEX idx_tags ((JSON_UNQUOTE(JSON_EXTRACT(tags, '$[*]')))), -- 函数索引优化json查询(mysql 8.0+ 支持)
    INDEX idx_userId (user_id),                                  -- 提升基于用户 ID 的查询性能
    INDEX idx_spaceId (space_id),                                -- 提升基于空间 ID 的查询性能
    INDEX idx_reviewStatus (review_status)                       -- 提升基于审核状态的查询性能
) comment '图片表' collate = utf8mb4_unicode_ci;

-- 5、空间目录表
drop table if exists spa_catalog;
create table spa_catalog
(
    id          bigint auto_increment comment '目录 id' primary key,
    space_id    bigint                             not null comment '空间 id',
    name        varchar(128)                       not null comment '目录名称',
    create_time datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    delete_time datetime DEFAULT null COMMENT '逻辑删除：null-正常, 非null-删除时间',
    -- 索引设计
    UNIQUE KEY uk_spaceId_name (space_id, name), -- 确保同一空间内目录名称唯一
    INDEX idx_spaceId (space_id)                 -- 提升按空间查询的性能
) comment '空间目录' collate = utf8mb4_unicode_ci;

-- 6、空间表
drop table if exists space;
create table if not exists space
(
    id          bigint auto_increment comment 'id' primary key,
    user_id     bigint                             not null comment '创建用户 id',
    space_name  varchar(128)                       null comment '空间名称',
    space_level int      default 0                 null comment '空间级别：0-普通版 1-专业版 2-旗舰版',
    max_size    bigint   default 0                 null comment '空间图片的最大总大小',
    max_count   bigint   default 0                 null comment '空间图片的最大数量',
    total_size  bigint   default 0                 null comment '当前空间下图片的总大小',
    total_count bigint   default 0                 null comment '当前空间下的图片数量',
    create_time datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    delete_time datetime DEFAULT null COMMENT '逻辑删除：null-正常, 非null-删除时间',
    -- 索引设计
    index idx_userId (user_id),        -- 提升基于用户的查询效率
    index idx_spaceName (space_name),  -- 提升基于空间名称的查询效率
    index idx_spaceLevel (space_level) -- 提升按空间级别查询的效率
) comment '空间' collate = utf8mb4_unicode_ci;

-- 7、空间成员表
drop table if exists space_user;
create table if not exists space_user
(
    id          bigint auto_increment comment 'id' primary key,
    space_id    bigint                             not null comment '空间 id',
    user_id     bigint                             not null comment '用户 id',
    space_role  tinyint  default '3'               not null comment '空间角色：0-creator；1-admin；2-editor；3-viewer',
    status      tinyint  default '1'               not null comment '状态：0-禁用；1-启用',
    status_msg  varchar(256)                       null comment '状态信息',
    create_time datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    -- 索引设计
    UNIQUE KEY uk_spaceId_userId (space_id, user_id), -- 唯一索引，用户在一个空间中只能有一个角色
    INDEX idx_spaceId (space_id),                     -- 提升按空间查询的性能
    INDEX idx_userId (user_id)                        -- 提升按用户查询的性能
) comment '空间用户关联' collate = utf8mb4_unicode_ci;


