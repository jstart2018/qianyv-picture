package com.jstart.qypicture.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jstart.qypicture.model.dto.*;
import com.jstart.qypicture.model.entity.Blog;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jstart.qypicture.model.vo.BlogAdminQueryVO;
import com.jstart.qypicture.model.vo.BlogSimpleVO;
import com.jstart.qypicture.model.vo.BlogsVO;

import java.util.List;

/**
 * @author 28435
 * @description 针对表【blog(博客表)】的数据库操作Service
 * @createDate 2025-10-08 15:25:16
 */
public interface BlogService extends IService<Blog> {

    Long createBlog(BlogCreateDTO blogCreateDTO);

    void delBlog(Long id);

    List<BlogsVO> selectList(BlogListDTO blogListDTO);

    List<BlogsVO> selectListByPage(BlogListDTO blogListDTO);

    QueryWrapper<Blog> getQueryWrapper(BlogListDTO blogListDTO);

    /**
     * 收藏切换
     * @param id
     */
    void blogCollect(Long id);

    /**
     * 点赞切换
     * @param id
     */
    void blogLike(Long id);

    List<BlogsVO> getBlogVOList(List<Blog> blogList);

    /**
     * 博客分页查询（支持查询我发布的/我点赞的/我收藏的）
     * @param blogPageQueryDTO
     * @return
     */
    Page<BlogSimpleVO> selectBlogByPage(BlogPageQueryDTO blogPageQueryDTO);

    /**
     * 将Blog列表转换为BlogSimpleVO列表
     * @param blogList
     * @return
     */
    List<BlogSimpleVO> getBlogSimpleVOList(List<Blog> blogList);

    /**
     * 管理员查询博客分页
     * @param blogListDTO
     * @return
     */
    Page<BlogAdminQueryVO> adminBlogPage(BlogListDTO blogListDTO);

    /**
     * 博客审核
     * @param blogAuditDTO
     */
    void auditBlog(BlogAuditDTO blogAuditDTO);
}
