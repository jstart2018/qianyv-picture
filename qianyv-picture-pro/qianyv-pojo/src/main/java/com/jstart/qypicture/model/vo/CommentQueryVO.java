package com.jstart.qypicture.model.vo;

import com.jstart.qypicture.model.entity.Comment;
import com.jstart.qypicture.model.entity.User;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 评论表
 * @TableName common
 */
@Data
public class CommentQueryVO {
    /**
     * 评论id
     */
    private Long id;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 关联博客 id
     */
    private Long blogId;

    /**
     * 评论用户
     */
    private UserInfoVO userInfoVO;

    /**
     * 父评论，null 表示一级评论
     */
    private CommentQueryVO parentCommon;

    /**
     * 点赞数
     */
    private Integer likeCount;

    /**
     * 子评论
     */
    private List<CommentQueryVO> replies = new ArrayList<>();


    /**
     * 回复的用户 id，null 表示回复帖子本身
     */
    private UserInfoVO replyToUser;

    /**
     * 创建时间
     */
    private Date createTime;

    public static CommentQueryVO getVO(Comment comment){
        CommentQueryVO commentQueryVO = new CommentQueryVO();
        BeanUtils.copyProperties(comment, commentQueryVO);
        return commentQueryVO;
    }


}