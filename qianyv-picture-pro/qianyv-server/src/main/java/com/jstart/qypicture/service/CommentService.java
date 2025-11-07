package com.jstart.qypicture.service;

import com.jstart.qypicture.model.entity.Comment;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jstart.qypicture.model.vo.CommentQueryVO;

import java.util.List;

/**
* @author 28435
* @description 针对表【common(评论表)】的数据库操作Service
* @createDate 2025-11-07 09:57:43
*/
public interface CommentService extends IService<Comment> {

    List<CommentQueryVO> getCommentsByBlogId(Long blogId);

}
