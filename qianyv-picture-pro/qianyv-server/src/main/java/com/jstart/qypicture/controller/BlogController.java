package com.jstart.qypicture.controller;


import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jstart.qypicture.constant.RedisKey;
import com.jstart.qypicture.enums.ResultEnum;
import com.jstart.qypicture.exception.BusinessException;
import com.jstart.qypicture.model.dto.*;
import com.jstart.qypicture.model.entity.Blog;
import com.jstart.qypicture.model.vo.BlogSimpleVO;
import com.jstart.qypicture.model.vo.BlogsVO;
import com.jstart.qypicture.result.Result;
import com.jstart.qypicture.service.BlogService;
import com.jstart.qypicture.utils.ThrowUtils;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/blog")
public class BlogController {

    @Resource
    private BlogService blogService;
    @Resource
    private RedisTemplate redisTemplate;


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
    public Result<List<BlogsVO>> blogList(@RequestBody BlogListDTO blogListDTO) {
        ThrowUtils.throwIf(blogListDTO == null, ResultEnum.PARAMS_ERROR, "请求参数为空");

        List<BlogsVO> blogsVOList = blogService.selectList(blogListDTO);

        return Result.success(blogsVOList);
    }

    /**
     * 分页查询博客列表（支持查询我发布的/我点赞的/我收藏的）
     * @param blogPageQueryDTO
     * @return
     */
    @PostMapping("/list/page")
    public Result<Page<BlogSimpleVO>> blogListByPage(@RequestBody BlogPageQueryDTO blogPageQueryDTO) {
        ThrowUtils.throwIf(blogPageQueryDTO == null, ResultEnum.PARAMS_ERROR, "请求参数为空");

        Page<BlogSimpleVO> blogSimpleVOPage = blogService.selectBlogByPage(blogPageQueryDTO);

        return Result.success(blogSimpleVOPage);
    }

    /**
     * 获取博客详情
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public Result<BlogsVO> getBlogDetail(@PathVariable Long id) {
        ThrowUtils.throwIf(id == null, ResultEnum.PARAMS_ERROR, "参数错误");

        Blog blog = blogService.getById(id);
        ThrowUtils.throwIf(blog == null, ResultEnum.NOT_FOUND_ERROR, "博客不存在");

        return Result.success(blogService.getBlogVOList(List.of(blog)).get(0));
    }

    /**
     * 博客收藏切换
     *
     * @param blogLikeOrCollectDTO
     * @return
     */
    @PostMapping("/blogCollect")
    public Result<?> collectToggle(@RequestBody BlogLikeOrCollectDTO blogLikeOrCollectDTO) {
        ThrowUtils.throwIf(blogLikeOrCollectDTO == null, ResultEnum.PARAMS_ERROR, "参数错误");

        blogService.blogCollect(blogLikeOrCollectDTO.getBlogId());

        return Result.success();
    }

    /**
     * 博客点赞切换
     *
     * @param blogLikeOrCollectDTO
     * @return
     */
    @PostMapping("/blogLikes")
    public Result<?> likesToggle(@RequestBody BlogLikeOrCollectDTO blogLikeOrCollectDTO) {
        ThrowUtils.throwIf(blogLikeOrCollectDTO == null, ResultEnum.PARAMS_ERROR, "参数错误");

        blogService.blogLike(blogLikeOrCollectDTO.getBlogId());

        return Result.success();
    }

    @GetMapping("/checkLike")
    public Result<Boolean> checkBlogLike(@RequestParam Long blogId) {
        ThrowUtils.throwIf(blogId == null, ResultEnum.PARAMS_ERROR, "参数错误");
        Blog blog = blogService.getById(blogId);
        ThrowUtils.throwIf(blog == null, ResultEnum.NOT_FOUND_ERROR, "博客不存在");
        try {
            Boolean isMember = redisTemplate.opsForSet().isMember(RedisKey.BLOG_LIKE_KEY + blogId, StpUtil.getLoginIdAsLong());
            return Result.success(isMember);
        } catch (NotLoginException e) {
            return Result.success(Boolean.FALSE);
        }
    }

    @GetMapping("/checkCollect")
    public Result<Boolean> checkCollect(@RequestParam Long blogId) {
        ThrowUtils.throwIf(blogId == null, ResultEnum.PARAMS_ERROR, "参数错误");
        Blog blog = blogService.getById(blogId);
        ThrowUtils.throwIf(blog == null, ResultEnum.NOT_FOUND_ERROR, "博客不存在");
        try {
            Boolean isMember = redisTemplate.opsForSet().isMember(RedisKey.BLOG_COLLECTION_KEY + blogId, StpUtil.getLoginIdAsLong());
            return Result.success(isMember);
        } catch (NotLoginException e) {
            return Result.success(Boolean.FALSE);
        }
    }


}
