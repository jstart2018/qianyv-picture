package com.jstart.qypicture.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 关注表
 * @TableName follow
 */
@TableName(value ="follow")
@Data
public class Follow {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户 id
     */
    private Long userId;

    /**
     * 被关注用户 id
     */
    private Long followUserId;

    /**
     * 创建时间
     */
    private Date createTime;
}