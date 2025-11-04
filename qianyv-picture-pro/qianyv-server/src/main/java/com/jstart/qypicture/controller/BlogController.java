package com.jstart.qypicture.controller;


import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jstart.qypicture.enums.ResultEnum;
import com.jstart.qypicture.exception.BusinessException;
import com.jstart.qypicture.model.dto.*;
import com.jstart.qypicture.model.entity.Blog;
import com.jstart.qypicture.model.vo.BlogCommentVO;
import com.jstart.qypicture.model.vo.BlogLikeOrCollectVO;
import com.jstart.qypicture.model.vo.BlogsVO;
import com.jstart.qypicture.result.Result;
import com.jstart.qypicture.service.BlogService;
import com.jstart.qypicture.utils.ThrowUtils;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/blog")
public class BlogController {

    @Resource
    private BlogService blogService;

    @PostMapping("/add")
    public Result<Long> addBlog(@RequestBody BlogCreateDTO blogCreateDTO) {
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
    public Result<Page<BlogsVO>> blogList(@RequestBody BlogListDTO blogListDTO) {
        ThrowUtils.throwIf(blogListDTO == null, ResultEnum.PARAMS_ERROR, "请求参数为空");

        Page<BlogsVO> blogListVOPage = blogService.selectList(blogListDTO);

        return Result.success(blogListVOPage);
    }

    @GetMapping("/{id}")
    public Result<BlogsVO> getBlogDetail(@PathVariable Long id) {
        ThrowUtils.throwIf(id == null, ResultEnum.PARAMS_ERROR, "参数错误");

        Blog blog = blogService.getById(id);
        ThrowUtils.throwIf(blog == null, ResultEnum.NOT_FOUND_ERROR, "博客不存在");

        BlogsVO blogsVO = new BlogsVO();
        BeanUtils.copyProperties(blog, blogsVO);

        return Result.success(blogsVO);
    }

    @GetMapping("/likes/toggle")
    public Result<BlogLikeOrCollectVO> likeToggle(@RequestBody BlogLikeOrCollectDTO blogLikeOrCollectDTO) {
        ThrowUtils.throwIf(blogLikeOrCollectDTO == null, ResultEnum.PARAMS_ERROR, "参数错误");

        Blog blog = blogService.getById(blogLikeOrCollectDTO.getId());
        ThrowUtils.throwIf(blog == null, ResultEnum.NOT_FOUND_ERROR, "博客不存在");

        BlogLikeOrCollectVO blogLikeOrCollectVO = new BlogLikeOrCollectVO();
        blogLikeOrCollectVO.setCount(5L);
        blogLikeOrCollectVO.setHaveBean(true);

        return Result.success(blogLikeOrCollectVO);
    }

    @GetMapping("/collections/toggle")
    public Result<BlogLikeOrCollectVO> collectionsToggle(@RequestBody BlogLikeOrCollectDTO blogLikeOrCollectDTO) {
        ThrowUtils.throwIf(blogLikeOrCollectDTO == null, ResultEnum.PARAMS_ERROR, "参数错误");

        Blog blog = blogService.getById(blogLikeOrCollectDTO.getId());
        ThrowUtils.throwIf(blog == null, ResultEnum.NOT_FOUND_ERROR, "博客不存在");

        BlogLikeOrCollectVO blogLikeOrCollectVO = new BlogLikeOrCollectVO();
        blogLikeOrCollectVO.setCount(5L);
        blogLikeOrCollectVO.setHaveBean(true);

        return Result.success(blogLikeOrCollectVO);
    }

    @GetMapping("/comments")
    public Result<BlogCommentVO> comments(@RequestBody BlogCommentDTO blogCommentDTO) {
        ThrowUtils.throwIf(blogCommentDTO == null, ResultEnum.PARAMS_ERROR, "参数错误");

        BlogCommentVO bean = JSONUtil.toBean("{\n" +
                "  \"id\": 500542521,             \n" +
                "  \"commentId\": 101010101010,    \n" +
                "  \"userId\": 101,          \n" +
                "  \"username\": \"张三\",     \n" +
                "  \"userAvatar\": null, \n" +
                "  \"content\": \"这篇文章写得真好！\",   \n" +
                "  \"createTime\": \"2025-10-28 15:30:00\", \n" +
                "  \"likeCount\": 0,         \n" +
                "  \"parentId\": null,       \n" +
                "  \"parentUsername\": null  \n" +
                "}", BlogCommentVO.class);

        return Result.success(bean);
    }

}
