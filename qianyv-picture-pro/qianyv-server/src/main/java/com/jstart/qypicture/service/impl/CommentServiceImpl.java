package com.jstart.qypicture.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jstart.qypicture.enums.ResultEnum;
import com.jstart.qypicture.model.entity.Comment;
import com.jstart.qypicture.model.entity.User;
import com.jstart.qypicture.model.vo.CommentQueryVO;
import com.jstart.qypicture.model.vo.UserInfoVO;
import com.jstart.qypicture.service.CommentService;
import com.jstart.qypicture.mapper.CommentMapper;
import com.jstart.qypicture.service.UserService;
import com.jstart.qypicture.utils.ThrowUtils;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
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
        // 1、查询该帖子的所有顶级评论（parent_id = null）
        List<Comment> topLevelComments = lambdaQuery().eq(Comment::getBlogId, blogId)
                .isNull(Comment::getParentId)
                .orderByDesc(Comment::getCreateTime) // 按创建时间降序排列
                .list();
        if (topLevelComments== null || topLevelComments.isEmpty()) {
            return List.of(); // 返回空列表
        }

        ArrayList<CommentQueryVO> allCommentVOs = new ArrayList<>();//最终返回的集合

        //2、遍历顶级评论，查询其下的所有子评论，构建完整评论组
        topLevelComments.forEach(topComment -> {
            //（1）当前父评论
            CommentQueryVO topCommentVo = CommentQueryVO.getVO(topComment);
            //填充父评论的用户信息
            topCommentVo.setUserInfoVO(userService.getUserInfoVO(userService.getById(topComment.getUserId())));

            //（2）查该父级评论下的所有子评论
            List<Comment> childComment = lambdaQuery().eq(Comment::getParentId, topCommentVo.getId())
                    .orderBy(true, true, Comment::getCreateTime) // 按创建时间升序排列
                    .list();

            //遍历子评论，填充信息
            ArrayList<CommentQueryVO> childCommentVOs = new ArrayList<>();  //所有最终返回的子评论
            for (Comment child : childComment) {
                //单个子评论
                CommentQueryVO childVO = CommentQueryVO.getVO(child);
                //填充子评论的用户信息
                UserInfoVO childUserInfoVO = userService.getUserInfoVO(userService.getById(child.getUserId()));
                childVO.setUserInfoVO(childUserInfoVO);
                //设置回复的用户信息
                if ( child.getReplyUserId() != null){
                    User user = userService.getById(child.getReplyUserId());
                    ThrowUtils.throwIf(user == null, ResultEnum.NOT_FOUND_ERROR,"回复的用户不存在或已被删除");
                    UserInfoVO replyToUserInfoVO = userService.getUserInfoVO(user);
                    childVO.setReplyToUser(replyToUserInfoVO);
                }
                childCommentVOs.add(childVO);

            }
            //将 所有子评论 的内容都放到父评论下面
            topCommentVo.setReplies(childCommentVOs);
            // 父评论集合
            allCommentVOs.add(topCommentVo);
        });

        return allCommentVOs;
    }



}




