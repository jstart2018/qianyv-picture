package com.jstart.qypicture.controller;


import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jstart.qypicture.enums.ResultEnum;
import com.jstart.qypicture.exception.BusinessException;
import com.jstart.qypicture.model.dto.BlogCreateDTO;
import com.jstart.qypicture.model.dto.BlogEditDTO;
import com.jstart.qypicture.model.dto.BlogListDTO;
import com.jstart.qypicture.model.entity.Blog;
import com.jstart.qypicture.model.vo.BlogListVO;
import com.jstart.qypicture.result.Result;
import com.jstart.qypicture.service.BlogService;
import com.jstart.qypicture.utils.ThrowUtils;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/blog")
public class BlogController {

    @Resource
    private BlogService blogService;

    @GetMapping
    public Result<?> getBlogById(Long id) {

        return Result.success();
    }

    @PostMapping("/create")
    public Result<Long> createBlog(@RequestBody BlogCreateDTO blogCreateDTO) {
        ThrowUtils.throwIf(blogCreateDTO == null, ResultEnum.PARAMS_ERROR, "参数错误");
        Long blogId = blogService.createBlog(blogCreateDTO);
        return Result.success(blogId);
    }


    @PostMapping("/edit")
    public Result<?> editBlog(@RequestBody BlogEditDTO blogEditDTO) {
        ThrowUtils.throwIf(blogEditDTO == null || blogEditDTO.getId() == null,
                ResultEnum.PARAMS_ERROR, "参数错误");

        Blog b = blogService.getById(blogEditDTO.getId());
        ThrowUtils.throwIf(b == null, ResultEnum.NOT_FOUND_ERROR, "博客不存在");
        if (b.getId().equals(StpUtil.getLoginIdAsLong())) {
            throw new BusinessException(ResultEnum.NO_AUTH_ERROR, "仅本人可编辑");
        }

        Blog blog = new Blog();
        BeanUtils.copyProperties(blogEditDTO, blog);

        boolean result = blogService.updateById(blog);
        ThrowUtils.throwIf(!result, ResultEnum.SYSTEM_ERROR, "编辑失败，请稍后再试");

        return Result.success();

    }

    @DeleteMapping("/del")
    public Result<?> delBlog(@RequestParam Long id) {
        ThrowUtils.throwIf(id == null, ResultEnum.PARAMS_ERROR, "参数错误");

        blogService.delBlog(id);

        return Result.success("删除成功");

    }

    @PostMapping("/list")
    public Result<Page<BlogListVO>> blogList(@RequestBody BlogListDTO blogListDTO) {
        ThrowUtils.throwIf(blogListDTO == null, ResultEnum.PARAMS_ERROR, "请求参数为空");

        Page<BlogListVO> blogListVOPage = blogService.selectList(blogListDTO);

        return Result.success(blogListVOPage);
    }

    @GetMapping("/{id}")
    public Result<BlogListVO> getBlogDetail(@PathVariable Long id) {
        ThrowUtils.throwIf(id == null, ResultEnum.PARAMS_ERROR, "参数错误");

        Blog blog = blogService.getById(id);
        ThrowUtils.throwIf(blog == null, ResultEnum.NOT_FOUND_ERROR, "博客不存在");

        BlogListVO blogListVO = new BlogListVO();
        BeanUtils.copyProperties(blog, blogListVO);

        return Result.success(blogListVO);
    }

}
