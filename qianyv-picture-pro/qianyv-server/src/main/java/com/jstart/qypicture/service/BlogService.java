package com.jstart.qypicture.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jstart.qypicture.model.dto.BlogCreateDTO;
import com.jstart.qypicture.model.dto.BlogListDTO;
import com.jstart.qypicture.model.entity.Blog;
import com.baomidou.mybatisplus.extension.service.IService;
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

    Page<BlogsVO> selectList(BlogListDTO blogListDTO);

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
}
