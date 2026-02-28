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
    user_id        bigint                                 not null comment '创建用户 id',
    blog_id        bigint comment '关联博客id',
    url            varchar(512)                           not null comment '图片 url',
    thumb_url      varchar(512)                           not null comment '缩略图 url',
    introduction   varchar(512)                           null comment '简介',
    category_id    bigint comment '分类id',
    tags           varchar(512)                           null comment '标签（JSON 数组）',
    pic_size       bigint                                 null comment '图片体积',
    pic_width      int                                    null comment '图片宽度',
    pic_height     int                                    null comment '图片高度',
    pic_scale      double                                 null comment '图片宽高比例',
    pic_format     varchar(32)                            null comment '图片格式',
    collect_count  bigint       default 0                 not null comment '收藏数量',
    upload_status  tinyint      DEFAULT 0                 NOT NULL COMMENT '上传状态：0-preview预览; 1-formal正式',
    is_recommend   tinyint      DEFAULT 0                 NOT NULL COMMENT '是否精选：0-否; 1-是',
    review_status  tinyint      DEFAULT 0                 NOT NULL COMMENT '审核状态：0-待审核; 1-通过; 2-拒绝',
    review_message VARCHAR(512) DEFAULT '未审核'          NOT NULL COMMENT '审核信息',
    reviewer_id    BIGINT                                 NULL COMMENT '审核人 ID',
    review_time    datetime     default NULL COMMENT '审核时间',
    create_time    datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time    datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    delete_time    datetime     DEFAULT NULL COMMENT '逻辑删除：null-正常, 非null-删除时间',
    INDEX idx_blogId (blog_id),             -- 提升基于博客 ID 的查询性能
    INDEX idx_tags (tags),                  -- 提升基于标签的查询性能
    INDEX idx_category (category_id),       -- 提升基于分类的查询性能
    INDEX idx_userId (user_id),             -- 提升基于用户 ID 的查询性能
    INDEX idx_uploadStatus (upload_status), -- 提升基于上传状态的查询性能
    INDEX idx_reviewStatus (review_status)  -- 提升基于审核状态的查询性能
) comment '公共图片表' collate = utf8mb4_unicode_ci;

ALTER TABLE pub_picture
    ADD COLUMN download_count bigint default 0 comment "下载量" AFTER collect_count;
ALTER TABLE pub_picture
    ADD COLUMN ai_desc VARCHAR(500) default '' COMMENT 'AI图片描述' AFTER introduction;

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
drop table if exists spa_picture;
create table spa_picture
(
    id             bigint auto_increment comment 'id' primary key,
    name           varchar(512)                           not null comment '图片名称',
    user_id        bigint                                 not null comment '创建用户 id',
    space_id       bigint       default 0                 not null comment '所属空间id',
    catalog_id     bigint                                 not null comment '目录 id',
    url            varchar(512)                           not null comment '图片 url',
    thumb_url      varchar(512)                           not null comment '缩略图 url',
    introduction   varchar(512)                           null comment '简介',
    tags           varchar(512)                           null comment '标签（JSON 数组）',
    pic_size       bigint                                 null comment '图片体积',
    pic_width      int                                    null comment '图片宽度',
    pic_height     int                                    null comment '图片高度',
    pic_scale      double                                 null comment '图片宽高比例',
    pic_format     varchar(32)                            null comment '图片格式',
    upload_status  tinyint      DEFAULT 0                 NOT NULL COMMENT '上传状态：0-preview预览; 1-formal正式',
    review_status  tinyint      DEFAULT 0                 NOT NULL COMMENT '审核状态：0-待审核; 1-临时通过; 2-正式通过; 3-拒绝',
    review_message VARCHAR(512) DEFAULT '未审核'          NOT NULL COMMENT '审核信息',
    reviewer_id    BIGINT       DEFAULT 0                 NOT NULL COMMENT '审核人 ID，0-表示系统自动审核',
    review_time    datetime     default NULL COMMENT '审核时间',
    create_time    datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time    datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    delete_time    datetime     DEFAULT null COMMENT '逻辑删除：null-正常, 非null-删除时间',
    INDEX idx_tags (tags),                 -- 提升基于标签的查询性能
    INDEX idx_userId (user_id),            -- 提升基于用户 ID 的查询性能
    INDEX idx_spaceId (space_id),          -- 提升基于空间 ID 的查询性能
    INDEX idx_reviewStatus (review_status) -- 提升基于审核状态的查询性能
) comment '空间图片表' collate = utf8mb4_unicode_ci;

ALTER TABLE spa_picture
    ADD COLUMN download_count bigint default 0 comment "下载量" AFTER pic_format;

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
    space_level int      default 0                 null comment '空间级别：0-免费版 1-普通版 2-专业版 3-旗舰版',
    status      tinyint  default 1                 null comment '状态：0-禁用 1-启用',
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

-- 8、博客表
drop table if exists blog;
CREATE TABLE `blog`
(
    `id`            bigint                                 NOT NULL AUTO_INCREMENT COMMENT '文章ID',
    `user_id`       bigint                                 NOT NULL COMMENT '作者ID',
    `title`         varchar(200)                           NOT NULL COMMENT '文章标题',
    `content`       text                                   NOT NULL COMMENT '文章内容',
    `view_count`    int                                    NOT NULL DEFAULT 0 COMMENT '浏览量',
    `like_count`    int                                    NOT NULL DEFAULT 0 COMMENT '点赞数',
    `comment_count` int                                    NOT NULL DEFAULT 0 COMMENT '评论数',
    collect_count   bigint       default 0                 not null comment '收藏数量',
    is_recommend    tinyint      DEFAULT 0                 NOT NULL COMMENT '是否精选：0-否; 1-是',
    sort            int          DEFAULT 99                NOT NULL COMMENT '排序权重（越小越前）',
    review_status   tinyint      DEFAULT 0                 NOT NULL COMMENT '审核状态：0-待审核; 1-通过; 2-拒绝',
    review_message  VARCHAR(512) DEFAULT '未审核'          NOT NULL COMMENT '审核信息',
    reviewer_id     BIGINT       DEFAULT 0                 NOT NULL COMMENT '审核人 ID',
    review_time     datetime     default NULL COMMENT '审核时间',
    create_time     datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time     datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    delete_time     datetime     DEFAULT NULL COMMENT '逻辑删除：null-正常, 非null-删除时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`) COMMENT '按作者查询文章',
    FULLTEXT KEY `idx_title_content` (`title`, `content`) COMMENT '标题+内容全文检索'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='博客表';

-- 9、评论表
drop table if exists comment;
create table comment
(
    id            bigint auto_increment primary key,
    content       varchar(64)                        not null comment '评论内容',
    blog_id       bigint                             not null comment '关联博客 id',
    user_id       bigint                             not null comment '评论用户 id',
    parent_id     bigint                             null comment '父评论 id，null 表示一级评论',
    reply_user_id bigint                             null comment '回复的用户id',
    like_count    bigint   default 0                 not null comment '点赞数',
    create_time   datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time   datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    delete_user   bigint   DEFAULT null COMMENT '删除人id',
    delete_time   datetime DEFAULT null COMMENT '逻辑删除：null-正常, 非null-删除时间',
    -- 索引设计
    index idx_blogId (blog_id),            -- 提升按博客查询评论的性能
    index idx_userId (user_id),            -- 提升按用户查询评论的性能
    index idx_parentId (parent_id),        -- 提升按父评论查询子评论的性能
    index idx_replyUserId (reply_user_id), -- 提升按父评论查询子评论的性能
    index idx_deleteUser (delete_user)     -- 提升按删除人查询评论的性能
) comment '评论表' collate = utf8mb4_unicode_ci;

-- 10、收藏表
drop table if exists collection;
create table collection
(
    id           bigint auto_increment primary key,
    user_id      bigint                             not null comment '用户 id',
    target_id    bigint                             not null comment '收藏目标 id',
    content_type tinyint                            not null comment '内容类型：0-图片; 1-博客',
    create_time  datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    UNIQUE KEY uk_userId_pictureId (user_id, target_id, content_type), -- 确保用户对同一图片只能收藏一次
    INDEX idx_userId (user_id),                                        -- 提升按用户查询收藏的性能
    INDEX idx_pictureId (target_id)                                    -- 提升按图片查询收藏的性能
) comment '收藏表' collate = utf8mb4_unicode_ci;

-- 11、关注表
drop table if exists follow;
create table follow
(
    id             bigint auto_increment primary key,
    user_id        bigint                             not null comment '用户 id',
    follow_user_id bigint                             not null comment '被关注用户 id',
    create_time    datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    UNIQUE KEY uk_userId_followId (user_id, follow_user_id), -- 确保用户对同一用户只能关注一次
    INDEX idx_userId (user_id),                              -- 提升按用户查询关注的性能
    INDEX idx_followId (follow_user_id)                      -- 提升按被关注用户查询关注的性能
) comment '关注表' collate = utf8mb4_unicode_ci;;

CREATE TABLE `ai_picture`
(
    `id`          bigint       NOT NULL AUTO_INCREMENT COMMENT '主键ID，自增唯一标识',
    `url`         varchar(512) NOT NULL COMMENT '图片存储URL（支持长链接，最大512字符）',
    `user_id`     bigint       NOT NULL COMMENT '关联的用户ID（与用户表主键关联）',
    `space_id`    bigint                DEFAULT null COMMENT '空间id（null表示用户个人图片）',
    `create_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间（默认当前时间，无需手动插入）',
    `status`      tinyint      NOT NULL DEFAULT 0 COMMENT '状态：0=生成，1=已转移',
    delete_time   datetime              DEFAULT null COMMENT '逻辑删除：null-正常, 非null-删除时间',
    PRIMARY KEY (`id`) USING BTREE,
    KEY `idx_user_id` (`user_id`) USING BTREE,        -- 按用户查询图片时加速
    KEY `idx_status` (`status`) USING BTREE,          -- 按状态筛选时加速
    KEY `idx_create_time` (`create_time`) USING BTREE -- 按时间排序/查询时加速
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='图片信息表';

