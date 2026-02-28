package com.jstart.qypicture.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jstart.qypicture.auth.SystemRoleEnum;
import com.jstart.qypicture.constant.RedisKey;
import com.jstart.qypicture.enums.BlogAuditEnum;
import com.jstart.qypicture.enums.CollectionEnum;
import com.jstart.qypicture.enums.ResultEnum;
import com.jstart.qypicture.exception.BusinessException;
import com.jstart.qypicture.mapper.PubPictureMapper;
import com.jstart.qypicture.model.dto.*;
import com.jstart.qypicture.model.entity.*;
import com.jstart.qypicture.model.vo.*;
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

import java.util.*;
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
    @Resource
    private PicCategoryService picCategoryService;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createBlog(BlogCreateDTO blogCreateDTO) {
        ThrowUtils.throwIf(blogCreateDTO.getTitle().length() > 15, ResultEnum.OPERATION_ERROR, "标题不可超过15个字符");
        ThrowUtils.throwIf(blogCreateDTO.getContent().length() > 100, ResultEnum.OPERATION_ERROR, "正文不可超过100个字符");

        // 1、创建博客
        Blog blog = new Blog();
        BeanUtils.copyProperties(blogCreateDTO, blog);
        blog.setUserId(StpUtil.getLoginIdAsLong());
        boolean result = this.save(blog);
        if (!result) {
            log.error("创建博客失败, 数据库操作博客表失败");
            throw new BusinessException(ResultEnum.SYSTEM_ERROR, "创建失败，请稍后再试");
        }

        // 2、更改图片表
        List<PictureEditDTO> pictureEditDTOList = blogCreateDTO.getPictureEditDTOList();
        for (PictureEditDTO pictureEditDTO : pictureEditDTOList) {
            pictureEditDTO.setBlogId(blog.getId());
            pictureEditDTO.setSpaceId(null);
            //如果没有设置标签就设置为分类名
            if (StrUtil.isBlank(pictureEditDTO.getTags())) {
                PicCategory picCategory = picCategoryService.getById(pictureEditDTO.getCategoryId());
                pictureEditDTO.setTags(picCategory.getCategoryName());
            }
            pictureService.edit(pictureEditDTO);
        }

        // 3、刷新贡献值
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

    /**
     * 博客游标查询
     *
     * @param blogListDTO
     * @return
     */
    @Override
    public List<BlogsVO> selectList(BlogListDTO blogListDTO) {
        blogListDTO.setReviewStatus(BlogAuditEnum.PASS.getValue());
        QueryWrapper<Blog> queryWrapper = getQueryWrapper(blogListDTO);

        //每次查五条
        Page<Blog> blogPage = this.page(new Page<>(1, 5), queryWrapper);
        List<Blog> blogList = blogPage.getRecords();

        //封装VO
        List<BlogsVO> blogsVOList = getBlogVOList(blogList);

        return blogsVOList;

    }

    /**
     * 博客分页查询
     *
     * @param blogListDTO
     * @return
     */
    @Override
    public List<BlogsVO> selectListByPage(BlogListDTO blogListDTO) {
        blogListDTO.setReviewStatus(BlogAuditEnum.PASS.getValue());
        QueryWrapper<Blog> queryWrapper = getQueryWrapper(blogListDTO);

        //分页查询
        Page<Blog> blogPage = this
                .page(new Page<>(blogListDTO.getCurrent(), blogListDTO.getPageSize()), queryWrapper);
        List<Blog> blogList = blogPage.getRecords();

        //封装VO
        List<BlogsVO> blogsVOList = getBlogVOList(blogList);

        return blogsVOList;

    }

    @Override
    public QueryWrapper<Blog> getQueryWrapper(BlogListDTO blogListDTO) {
        Long id = blogListDTO.getId();
        Date updateTime = blogListDTO.getUpdateTime();
        Long userId = blogListDTO.getUserId();
        String searchText = blogListDTO.getSearchText();
        Integer isRecommend = blogListDTO.getIsRecommend();
        Integer sort = blogListDTO.getSort();
        Integer reviewStatus = blogListDTO.getReviewStatus();
        String reviewMessage = blogListDTO.getReviewMessage();
        Long reviewerId = blogListDTO.getReviewerId();
        Boolean upToDate = blogListDTO.getUpToDate();

        QueryWrapper<Blog> qw = new QueryWrapper<>();

        qw.lt(id != null, "id", id); //游标

        qw.eq(userId != null, "user_id", userId);
        if (searchText != null) {
            // and() 表示：外层条件（id+userId）AND (title like OR content like)
            qw.and(wrapper -> wrapper
                    .like("title", searchText)  // 标题模糊查
                    .or()                       // 或
                    .like("content", searchText) // 内容模糊查
            );
        }
        qw.eq(isRecommend != null, "is_recommend", isRecommend);
        qw.eq(reviewStatus != null, "review_status", reviewStatus);
        qw.eq(reviewMessage != null, "review_message", reviewMessage);
        qw.eq(reviewerId != null, "reviewer_id", reviewerId);
        qw.orderBy(true, false, "id");
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
            if (addResult) {
                redisTemplate.opsForSet().remove(RedisKey.BLOG_LIKE_KEY + id, loginUserId);
                // 从用户维度的点赞key中移除博客ID
                redisTemplate.opsForSet().remove(RedisKey.USER_BLOG_LIKE_KEY + loginUserId, id);
            }
        } else {
            //未点赞，点赞
            boolean removeResult = this.lambdaUpdate().set(Blog::getLikeCount, blog.getLikeCount() + 1)
                    .eq(Blog::getId, id)
                    .update();
            if (removeResult) {
                redisTemplate.opsForSet().add(RedisKey.BLOG_LIKE_KEY + id, loginUserId);
                // 向用户维度的点赞key中添加博客ID
                redisTemplate.opsForSet().add(RedisKey.USER_BLOG_LIKE_KEY + loginUserId, id);
            }
        }

    }

    public List<BlogsVO> getBlogVOList(List<Blog> blogList) {

        if (blogList == null || blogList.size() == 0) {
            return Collections.emptyList();
        }
        //非管理员过滤掉未审核的博客
        if (!StpUtil.hasRole(SystemRoleEnum.ADMIN.getValue())) {
            blogList = blogList.stream()
                    .filter(blog -> blog.getReviewStatus() == BlogAuditEnum.PASS.getValue())
                    .collect(Collectors.toList());
        }

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

    /**
     * 博客分页查询（支持查询我发布的/我点赞的/我收藏的）
     *
     * @param dto
     * @return
     */
    @Override
    public Page<BlogSimpleVO> selectBlogByPage(BlogPageQueryDTO dto) {
        Long userId = dto.getUserId();
        Boolean myLike = dto.getMyLike();
        Boolean myCollect = dto.getMyCollect();
        String searchText = dto.getSearchText();
        Integer isRecommend = dto.getIsRecommend();
        Integer reviewStatus = BlogAuditEnum.PASS.getValue();

        QueryWrapper<Blog> qw = new QueryWrapper<>();
        List<Long> blogIds = null;

        // 查询我点赞的博客
        if (BooleanUtils.isTrue(myLike)) {
            long loginUserId = StpUtil.getLoginIdAsLong();
            Set<Object> likedBlogIds = redisTemplate.opsForSet().members(RedisKey.USER_BLOG_LIKE_KEY + loginUserId);
            if (likedBlogIds == null || likedBlogIds.isEmpty()) {
                Page<BlogSimpleVO> emptyPage = new Page<>(dto.getCurrent(), dto.getPageSize(), 0);
                emptyPage.setRecords(Collections.emptyList());
                return emptyPage;
            }
            blogIds = likedBlogIds.stream()
                    .map(obj -> Long.valueOf(obj.toString()))
                    .collect(Collectors.toList());
        }

        // 查询我收藏的博客
        if (BooleanUtils.isTrue(myCollect)) {
            long loginUserId = StpUtil.getLoginIdAsLong();
            Set<Object> collectedBlogIds = redisTemplate.opsForSet().members(RedisKey.USER_BLOG_COLLECTION_KEY + loginUserId);
            if (collectedBlogIds == null || collectedBlogIds.isEmpty()) {
                Page<BlogSimpleVO> emptyPage = new Page<>(dto.getCurrent(), dto.getPageSize(), 0);
                emptyPage.setRecords(Collections.emptyList());
                return emptyPage;
            }
            blogIds = collectedBlogIds.stream()
                    .map(obj -> Long.valueOf(obj.toString()))
                    .collect(Collectors.toList());
        }

        // 如果有指定博客ID列表，添加in条件
        if (blogIds != null && !blogIds.isEmpty()) {
            qw.in("id", blogIds);
        }

        // 查询我发布的博客
        qw.eq(userId != null, "user_id", userId);

        // 搜索条件
        if (StrUtil.isNotBlank(searchText)) {
            qw.and(wrapper -> wrapper
                    .like("title", searchText)
                    .or()
                    .like("content", searchText)
            );
        }

        qw.eq(isRecommend != null, "is_recommend", isRecommend);
        qw.eq(reviewStatus != null, "review_status", reviewStatus);
        qw.orderBy(true, false, "id");

        // 分页查询
        Page<Blog> blogPage = this.page(new Page<>(dto.getCurrent(), dto.getPageSize()), qw);
        List<Blog> blogList = blogPage.getRecords();

        // 封装简化版VO
        List<BlogSimpleVO> blogSimpleVOList = getBlogSimpleVOList(blogList);

        // 返回分页对象
        Page<BlogSimpleVO> voPage = new Page<>(blogPage.getCurrent(), blogPage.getSize(), blogPage.getTotal());
        voPage.setRecords(blogSimpleVOList);
        return voPage;
    }

    /**
     * 将Blog列表转换为BlogSimpleVO列表（不包含图片）
     *
     * @param blogList
     * @return
     */
    @Override
    public List<BlogSimpleVO> getBlogSimpleVOList(List<Blog> blogList) {
        if (blogList == null || blogList.isEmpty()) {
            return Collections.emptyList();
        }

        List<BlogSimpleVO> blogSimpleVOList = blogList.stream().map(blog -> {
            Long id = blog.getId();
            BlogSimpleVO blogSimpleVO = new BlogSimpleVO();
            BeanUtils.copyProperties(blog, blogSimpleVO);

            // 查询收藏数
            Long collectCount = redisTemplate.opsForSet().size(RedisKey.BLOG_COLLECTION_KEY + id);
            blogSimpleVO.setCollectCount(collectCount);

            // 查询评论数
            Long commentCount = commentService.lambdaQuery().eq(Comment::getBlogId, id).count();
            blogSimpleVO.setCommentCount(commentCount);

            return blogSimpleVO;
        }).collect(Collectors.toList());

        // 获取博客作者信息
        Set<Long> blogAuthors = blogList.stream().map(Blog::getUserId).collect(Collectors.toSet());
        List<User> users = userservice.listByIds(blogAuthors);
        Map<Long, User> userMap = users.stream()
                .collect(Collectors.toMap(User::getId, user -> user));

        // 填充作者信息
        blogSimpleVOList.forEach(blogSimpleVO -> {
            User user = userMap.get(blogSimpleVO.getUserId());
            if (user != null) {
                BlogAuthorVO blogAuthorVO = new BlogAuthorVO();
                BeanUtils.copyProperties(user, blogAuthorVO);
                blogSimpleVO.setUser(blogAuthorVO);
            }
        });

        return blogSimpleVOList;
    }

    @Override
    public Page<BlogAdminQueryVO> adminBlogPage(BlogListDTO blogListDTO) {
        QueryWrapper<Blog> queryWrapper = getQueryWrapper(blogListDTO);
        Page<Blog> blogPage = this.page(new Page<>(blogListDTO.getCurrent(), blogListDTO.getPageSize()), queryWrapper);

        Page<BlogAdminQueryVO> voPage = new Page<>(blogPage.getCurrent(), blogPage.getSize(), blogPage.getTotal());
        voPage.setRecords(blogPage.getRecords().stream().map(blog -> {
            BlogAdminQueryVO blogAdminQueryVO = new BlogAdminQueryVO();
            BeanUtils.copyProperties(blog, blogAdminQueryVO);
            //填充审核人
            User user = userservice.getById(blog.getReviewerId());
            blogAdminQueryVO.setReviewerName(user.getNickname());
            return blogAdminQueryVO;
        }).collect(Collectors.toList()));

        return voPage;
    }

    @Override
    public void auditBlog(BlogAuditDTO blogAuditDTO) {
        Blog blog = this.getById(blogAuditDTO.getId());
        ThrowUtils.throwIf(blog == null, ResultEnum.NOT_FOUND_ERROR, "博客不存在");

        if (blogAuditDTO.getReviewMessage() == null){
            blogAuditDTO.setReviewMessage("无");
        }

        //审核信息填充
        if (BlogAuditEnum.getEnumByValue(blogAuditDTO.getReviewStatus()) == null) {
            throw new BusinessException(ResultEnum.PARAMS_ERROR, "审核状态不合法");
        }
        blog.setReviewStatus(blogAuditDTO.getReviewStatus());
        blog.setReviewerId(StpUtil.getLoginIdAsLong());
        blog.setReviewTime(new Date());
        blog.setReviewMessage(blogAuditDTO.getReviewMessage());
        //更新博客审核状态
        boolean result = this.updateById(blog);
        ThrowUtils.throwIf(!result, ResultEnum.SYSTEM_ERROR, "审核失败，请稍后再试");

        //同步更新图片审核状态
        this.getBlogVOList(List.of(blog)).get(0).getPictureVOList().forEach(pictureVO -> {
            PictureEditDTO pictureEditDTO = new PictureEditDTO();
            pictureEditDTO.setId(pictureVO.getId());
            pictureEditDTO.setReviewStatus(blogAuditDTO.getReviewStatus());
            pictureEditDTO.setReviewerId(StpUtil.getLoginIdAsLong());
            pictureEditDTO.setReviewTime(new Date());
            pictureEditDTO.setReviewMessage(blogAuditDTO.getReviewMessage()+"(跟随博客审核结果)");
            pictureService.edit(pictureEditDTO);
        });

    }
}




