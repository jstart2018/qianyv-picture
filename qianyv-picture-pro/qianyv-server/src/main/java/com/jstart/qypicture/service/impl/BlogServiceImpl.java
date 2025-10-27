package com.jstart.qypicture.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jstart.qypicture.enums.ResultEnum;
import com.jstart.qypicture.exception.BusinessException;
import com.jstart.qypicture.mapper.PubPictureMapper;
import com.jstart.qypicture.model.dto.BlogCreateDTO;
import com.jstart.qypicture.model.dto.BlogListDTO;
import com.jstart.qypicture.model.dto.PictureEditDTO;
import com.jstart.qypicture.model.entity.Blog;
import com.jstart.qypicture.model.entity.PubPicture;
import com.jstart.qypicture.model.vo.BlogListVO;
import com.jstart.qypicture.service.BlogService;
import com.jstart.qypicture.mapper.BlogMapper;
import com.jstart.qypicture.service.PictureService;
import com.jstart.qypicture.utils.ThrowUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
    public Page<BlogListVO> selectList(BlogListDTO blogListDTO) {
        QueryWrapper<Blog> queryWrapper = getQueryWrapper(blogListDTO);

        Page<Blog> blogPage  = new Page<>(blogListDTO.getCurrent(),blogListDTO.getPageSize());
        blogPage = this.page(blogPage, queryWrapper);

        Page<BlogListVO>  blogListVOPage = new Page<>(blogPage.getCurrent(),blogPage.getSize(),blogPage.getTotal());
        blogListVOPage.setRecords(this.getBlogVOList(blogPage.getRecords()));

        return blogListVOPage;
    }

    @Override
    public List<BlogListVO> getBlogVOList(List<Blog> blogList) {
        List<BlogListVO> blogListVOList = blogList.stream().map(blog -> {
            Long id = blog.getId();
            List<String> pictureIds = pubPictureMapper.selectList(
                            new QueryWrapper<PubPicture>().eq("blog_id", id))
                    .stream()
                    .map(PubPicture::getThumbUrl)
                    .toList();
            BlogListVO blogListVO = new BlogListVO();
            BeanUtils.copyProperties(blog, blogListVO);
            blogListVO.setPictureList(pictureIds);

            return blogListVO;
        }).toList();

        return blogListVOList;
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

        qw.eq(id !=null,"id",id);
        qw.eq(userId !=null,"userId",id);
        qw.like(searchText !=null,"title",searchText);
        qw.like(searchText !=null,"content",searchText);
        qw.eq(isRecommend !=null,"is_recommend",isRecommend);
        qw.eq(reviewStatus !=null,"review_status",reviewStatus);
        qw.eq(reviewMessage !=null,"review_message",reviewMessage);
        qw.eq(reviewerId !=null,"reviewer_id",reviewerId);
        qw.orderBy(upToDate !=null,!upToDate,"update_time");
        qw.orderBy(sort !=null,true,"sort");

        return qw;
    }
}




