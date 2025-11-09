package com.jstart.qypicture.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jstart.qypicture.constant.RedisKey;
import com.jstart.qypicture.enums.CollectionEnum;
import com.jstart.qypicture.enums.ResultEnum;
import com.jstart.qypicture.exception.BusinessException;
import com.jstart.qypicture.mapper.PubPictureMapper;
import com.jstart.qypicture.model.dto.BlogCreateDTO;
import com.jstart.qypicture.model.dto.BlogListDTO;
import com.jstart.qypicture.model.dto.PictureEditDTO;
import com.jstart.qypicture.model.entity.*;
import com.jstart.qypicture.model.vo.BlogAuthorVO;
import com.jstart.qypicture.model.vo.BlogsVO;
import com.jstart.qypicture.model.vo.PictureListVO;
import com.jstart.qypicture.result.Result;
import com.jstart.qypicture.service.*;
import com.jstart.qypicture.mapper.BlogMapper;
import com.jstart.qypicture.utils.ThrowUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author 28435
 * @description 针对表【blog(博客表)】的数据库操作Service实现
 * @createDate 2025-10-08 15:25:16
 */
@Service
@Slf4j
public class BlogServiceImpl extends ServiceImpl<BlogMapper, Blog>
        implements BlogService {

    @Resource
    private PictureService pictureService;
    @Resource
    private PubPictureMapper pubPictureMapper;
    @Resource
    private UserService userservice;
    @Resource
    @Lazy
    private CollectionService collectionService;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private CommentService commentService;




    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createBlog(BlogCreateDTO blogCreateDTO) {
        ThrowUtils.throwIf(blogCreateDTO.getTitle().length() > 15, ResultEnum.OPERATION_ERROR, "标题不可超过15个字符");
        ThrowUtils.throwIf(blogCreateDTO.getContent().length() > 100, ResultEnum.OPERATION_ERROR, "正文不可超过100个字符");

        // 创建博客
        Blog blog = new Blog();
        BeanUtils.copyProperties(blogCreateDTO, blog);
        blog.setUserId(StpUtil.getLoginIdAsLong());
        boolean result = this.save(blog);
        if (!result) {
            log.error("创建博客失败, 数据库操作博客表失败");
            throw new BusinessException(ResultEnum.SYSTEM_ERROR, "创建失败，请稍后再试");
        }

        // 更改图片表
        List<PictureEditDTO> pictureEditDTOList = blogCreateDTO.getPictureEditDTOList();
        for (PictureEditDTO pictureEditDTO : pictureEditDTOList) {
            pictureEditDTO.setBlogId(blog.getId());
            pictureEditDTO.setSpaceId(null);
            pictureService.edit(pictureEditDTO);
        }

        //刷新贡献值
        redisTemplate.opsForZSet().incrementScore(RedisKey.USER_CONTRIBUTION_RANK_KEY,
                StpUtil.getLoginIdAsLong(),
                1);

        return blog.getId();
    }

    @Override
    public void delBlog(Long id) {
        //1、权限
        Blog b = this.getById(id);
        ThrowUtils.throwIf(b == null, ResultEnum.NOT_FOUND_ERROR, "博客不存在");
        if (b.getId().equals(StpUtil.getLoginIdAsLong())) {
            throw new BusinessException(ResultEnum.NO_AUTH_ERROR, "仅本人可删除");
        }

        //2、删除博客
        boolean removeBlog = this.removeById(id);
        //3、删除关联图片
        List<Long> pictureIds = pubPictureMapper.selectList(
                        new QueryWrapper<PubPicture>().eq("blog_id", id))
                .stream()
                .map(PubPicture::getId)
                .toList();
        boolean removePicture = pictureService.delete(pictureIds, null);

        ThrowUtils.throwIf(!removeBlog || !removePicture, ResultEnum.SYSTEM_ERROR, "删除失败，请稍后再试");
    }

    @Override
    public Page<BlogsVO> selectList(BlogListDTO blogListDTO) {
        QueryWrapper<Blog> queryWrapper = getQueryWrapper(blogListDTO);

        Page<Blog> blogPage = new Page<>(blogListDTO.getCurrent(), blogListDTO.getPageSize());
        blogPage = this.page(blogPage, queryWrapper);


        Page<BlogsVO> blogListVOPage = new Page<>(blogPage.getCurrent(), blogPage.getSize(), blogPage.getTotal());

        blogListVOPage.setRecords(this.getBlogVOList(blogPage.getRecords()));

        return blogListVOPage;
    }

    @Override
    public QueryWrapper<Blog> getQueryWrapper(BlogListDTO blogListDTO) {
        Long id = blogListDTO.getId();
        Long userId = blogListDTO.getUserId();
        String searchText = blogListDTO.getSearchText();
        Integer isRecommend = blogListDTO.getIsRecommend();
        Integer sort = blogListDTO.getSort();
        Integer reviewStatus = blogListDTO.getReviewStatus();
        String reviewMessage = blogListDTO.getReviewMessage();
        Long reviewerId = blogListDTO.getReviewerId();
        Boolean upToDate = blogListDTO.getUpToDate();

        QueryWrapper<Blog> qw = new QueryWrapper<>();

        qw.eq(id != null, "id", id);
        qw.eq(userId != null, "userId", id);
        qw.like(searchText != null, "title", searchText);
        qw.like(searchText != null, "content", searchText);
        qw.eq(isRecommend != null, "is_recommend", isRecommend);
        qw.eq(reviewStatus != null, "review_status", reviewStatus);
        qw.eq(reviewMessage != null, "review_message", reviewMessage);
        qw.eq(reviewerId != null, "reviewer_id", reviewerId);
        qw.orderBy(upToDate != null, BooleanUtils.isFalse(upToDate), "update_time");
        qw.orderBy(sort != null, true, "sort");

        return qw;
    }

    /**
     * 博客收藏
     *
     * @param id
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void blogCollect(Long id) {

        Blog blog = this.getById(id);
        ThrowUtils.throwIf(blog == null, ResultEnum.NOT_FOUND_ERROR, "博客不存在");

        long loginUserId = StpUtil.getLoginIdAsLong();

        collectionService.collectionToggle(loginUserId, id, CollectionEnum.BLOG);

    }

    @Override
    public void blogLike(Long id) {
        long loginUserId = StpUtil.getLoginIdAsLong();
        Blog blog = this.getById(id);

        Boolean isMember = redisTemplate.opsForSet()
                .isMember(RedisKey.BLOG_LIKE_KEY + id, loginUserId);

        if (BooleanUtils.isTrue(isMember)) {
            //已点赞，取消点赞
            boolean addResult = this.lambdaUpdate().set(Blog::getLikeCount, blog.getLikeCount() - 1)
                    .eq(Blog::getId, id)
                    .update();
            if (addResult)
                redisTemplate.opsForSet().remove(RedisKey.BLOG_LIKE_KEY + id, loginUserId);
        } else {
            //未点赞，点赞
            boolean removeResult = this.lambdaUpdate().set(Blog::getLikeCount, blog.getLikeCount() + 1)
                    .eq(Blog::getId, id)
                    .update();
            if (removeResult)
                redisTemplate.opsForSet().add(RedisKey.BLOG_LIKE_KEY + id, loginUserId);
        }

    }

    private List<BlogsVO> getBlogVOList(List<Blog> blogList) {
        List<BlogsVO> blogsVOList = blogList.stream().map(blog -> {
            Long id = blog.getId();
            //查询博客携带的图片
            List<PictureListVO> pictureVOList = pubPictureMapper.selectList(
                            new QueryWrapper<PubPicture>().eq("blog_id", id))
                    .stream()
                    .map(pubPicture -> {
                        PictureListVO pictureListVO = new PictureListVO();
                        BeanUtils.copyProperties(pubPicture, pictureListVO);
                        return pictureListVO;
                    })
                    .toList();
            BlogsVO blogsVO = new BlogsVO();
            BeanUtils.copyProperties(blog, blogsVO);
            //查询收藏数
            Long collectCount = redisTemplate.opsForSet().size(RedisKey.BLOG_COLLECTION_KEY + id);
            blogsVO.setCollectCount(collectCount);
            //查询评论数
            Long commentCount = commentService.lambdaQuery().eq(Comment::getBlogId, id).count();
            blogsVO.setCommentCount(commentCount);

            blogsVO.setPictureVOList(pictureVOList);
            return blogsVO;
        }).toList();


        //获取博客作者信息
        List<BlogsVO> b = blogList.stream().map(blog -> {
            BlogsVO blogsVO = new BlogsVO();
            BeanUtils.copyProperties(blog, blogsVO);
            return blogsVO;
        }).collect(Collectors.toList());
        //提取作者id
        Set<Long> blogAuthors = b.stream().map(BlogsVO::getUserId).collect(Collectors.toSet());
        //去数据库查用户
        List<User> users = userservice.listByIds(blogAuthors);
        //将user塞进blogsVOList里的user属性
        Map<Long, User> userMap = users
                .stream()
                .collect(Collectors.toMap(User::getId, user -> user));
        //blogsVOList填充user
        blogsVOList.forEach(blogsVO -> {
            User user = userMap.get(blogsVO.getUserId());
            if (user != null) {
                BlogAuthorVO blogAuthorVO = new BlogAuthorVO();
                BeanUtils.copyProperties(user, blogAuthorVO);
                blogsVO.setUser(blogAuthorVO);
            }
        });

        return blogsVOList;
    }
}




