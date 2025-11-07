package com.jstart.qypicture.controller;


import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import com.jstart.qypicture.enums.ResultEnum;
import com.jstart.qypicture.model.dto.CommentAddDTO;
import com.jstart.qypicture.model.entity.Comment;
import com.jstart.qypicture.model.vo.CommentQueryVO;
import com.jstart.qypicture.result.Result;
import com.jstart.qypicture.service.CommentService;
import com.jstart.qypicture.utils.ThrowUtils;
import jakarta.annotation.Resource;
import org.joda.time.LocalDateTime;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/common")
public class CommonController {

    @Resource
    private CommentService commentService;

    /**
     * 创建评论
     *
     * @return
     */
    @PostMapping("/add")
    public Result addCommon(@RequestBody CommentAddDTO commentAddDTO) {
        ThrowUtils.throwIf(commentAddDTO == null, ResultEnum.PARAMS_ERROR);
        ThrowUtils.throwIf(commentAddDTO.getBlogId() == null, ResultEnum.PARAMS_ERROR, "评论目标不能为空");
        ThrowUtils.throwIf(commentAddDTO.getContent() == null, ResultEnum.PARAMS_ERROR, "评论内容不能为空");
        if (commentAddDTO.getParentId() != null) {
            // 回复评论，检查评论是否存在
            boolean isExist = commentService.query().eq("id", commentAddDTO.getParentId()).exists();
            ThrowUtils.throwIf(!isExist, ResultEnum.PARAMS_ERROR, "回复的评论不存在或已被删除");
        }
        Comment comment = BeanUtil.copyProperties(commentAddDTO, Comment.class);
        comment.setUserId(StpUtil.getLoginIdAsLong());
        boolean save = commentService.save(comment);
        ThrowUtils.throwIf(!save, ResultEnum.SYSTEM_ERROR, "评论失败，请稍后重试");
        return Result.success();
    }

    /**
     * 删除评论
     *
     * @return
     */
    @DeleteMapping("/remove")
    public Result removeCommon(@RequestParam Long id) {
        Comment comment = commentService.getById(id);
        ThrowUtils.throwIf(comment == null, ResultEnum.PARAMS_ERROR, "评论不存在或已被删除");
        //todo：鉴权（只有本人或作者可删除）

        //删除评论
        commentService.lambdaUpdate().
                eq(Comment::getId, id)
                .set(Comment::getDeleteTime, LocalDateTime.now())
                .set(Comment::getDeleteUser, StpUtil.getLoginIdAsLong())
                .update();
        return Result.success();
    }

    /**
     * 获取博客的顶级评论
     *
     * @return
     */
    @PostMapping("/listParentCommon")
    public Result<List<CommentQueryVO>> listParentCommon(@RequestParam Long blogId) {
        ThrowUtils.throwIf(blogId == null, ResultEnum.PARAMS_ERROR, "评论目标不能为空");

        List<CommentQueryVO> commentQueryVOList = commentService.getCommentsByBlogId(blogId);

        return Result.success(commentQueryVOList);
    }


}
