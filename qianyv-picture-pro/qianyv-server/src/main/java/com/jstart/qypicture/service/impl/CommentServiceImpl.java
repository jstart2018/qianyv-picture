package com.jstart.qypicture.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jstart.qypicture.model.entity.Comment;
import com.jstart.qypicture.model.vo.CommentQueryVO;
import com.jstart.qypicture.model.vo.UserInfoVO;
import com.jstart.qypicture.service.CommentService;
import com.jstart.qypicture.mapper.CommentMapper;
import com.jstart.qypicture.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
* @author 28435
* @description 针对表【common(评论表)】的数据库操作Service实现
* @createDate 2025-11-07 09:57:43
*/
@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment>
    implements CommentService {

    @Resource
    private UserService userService;

    // 对外接口：根据帖子ID获取完整评论树（顶级评论+所有子回复）
    @Override
    @Transactional(readOnly = true) // 只读事务，提升性能
    public List<CommentQueryVO> getCommentsByBlogId(Long blogId) {
        // 步骤1：查询该帖子的所有顶级评论（parent_id = null）
        List<Comment> topLevelComments = lambdaQuery().eq(Comment::getBlogId, blogId)
                .isNull(Comment::getParentId)
                .orderByDesc(Comment::getCreateTime) // 按创建时间降序排列
                .list();

        if (topLevelComments== null || topLevelComments.isEmpty()) {
            return List.of(); // 返回空列表
        }

        // 步骤2：对每个顶级评论，递归查询其所有子回复，并组装成树
        return topLevelComments.stream()
                .map(this::buildCommentTree) // 核心：递归构建评论树
                .collect(Collectors.toList());
    }


    private CommentQueryVO buildCommentTree(Comment comment) {
        CommentQueryVO commentVO = CommentQueryVO.getVO(comment);
        //查询评论的用户信息
        UserInfoVO userInfoVO = userService.getUserInfoVO(userService.getById(comment.getUserId()));
        commentVO.setUserInfoVO(userInfoVO);
        // 查询该评论的所有直接子评论
        List<Comment> childComments = lambdaQuery()
                .eq(Comment::getParentId, comment.getId())
                .orderByDesc(Comment::getCreateTime)
                .list();

        // 递归构建每个子评论的树形结构
        List<CommentQueryVO> childCommentVOs = childComments.stream()
                .map(this::buildCommentTree)
                .collect(Collectors.toList());

        commentVO.setReplies(childCommentVOs);
        return commentVO;
    }


}




