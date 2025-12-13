package com.jstart.qypicture.handler.picture;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jstart.qypicture.auth.SystemRoleEnum;
import com.jstart.qypicture.enums.PicturePlaceEnum;
import com.jstart.qypicture.enums.ResultEnum;
import com.jstart.qypicture.mapper.PubPictureMapper;
import com.jstart.qypicture.model.UploadPictureResult;
import com.jstart.qypicture.model.dto.PictureDownLoadDTO;
import com.jstart.qypicture.model.dto.PictureEditDTO;
import com.jstart.qypicture.model.dto.PictureQueryListDTO;
import com.jstart.qypicture.model.entity.PubPicture;
import com.jstart.qypicture.model.vo.PictureListVO;
import com.jstart.qypicture.model.vo.PictureUploadVO;
import com.jstart.qypicture.model.vo.PictureVO;
import com.jstart.qypicture.service.BlogService;
import com.jstart.qypicture.service.PicCategoryService;
import com.jstart.qypicture.template.uploadFileTemplate.PictureUploadTemplate;
import com.jstart.qypicture.utils.ThrowUtils;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class PubPictureHandler implements PictureHandler<PubPicture> {

    @Resource
    private PictureUploadTemplate filePictureUpload;
    @Resource
    private PictureUploadTemplate urlPictureUpload;
    @Resource
    private PubPictureMapper pubPictureMapper;
    @Resource
    private PicCategoryService picCategoryService;
    @Resource
    private BlogService blogService;

    @Override
    public PicturePlaceEnum getPicturePlaceEnum() {
        return PicturePlaceEnum.PUBLIC;
    }

    @Override
    public PictureUploadVO upload(Object inputSource, Long spaceId) {
        String uploadPathPrefix = String.format("public/%s", StpUtil.getLoginIdAsString());
        // 根据 inputSource 的类型区分上传方式
        PictureUploadTemplate pictureUploadTemplate = filePictureUpload;
        if (inputSource instanceof String) {
            pictureUploadTemplate = urlPictureUpload;
        }
        UploadPictureResult uploadPictureResult = pictureUploadTemplate.uploadPicture(inputSource, uploadPathPrefix);

        PubPicture pubPicture = new PubPicture();
        pubPicture.setUserId(StpUtil.getLoginIdAsLong());
        BeanUtils.copyProperties(uploadPictureResult, pubPicture);
        int insert = pubPictureMapper.insert(pubPicture);
        ThrowUtils.throwIf(insert <= 0, ResultEnum.SYSTEM_ERROR, "图片入库失败");

        PictureUploadVO pictureUploadVO = new PictureUploadVO();
        pictureUploadVO.setId(pubPicture.getId());
        pictureUploadVO.setThumbUrl(pubPicture.getThumbUrl());

        return pictureUploadVO;

    }

    @Override
    public List<String> delete(List<Long> ids, Long spaceId) {
        //1、图片是否存在
        List<PubPicture> pubPictures = pubPictureMapper.selectByIds(ids);
        ThrowUtils.throwIf(pubPictures == null || pubPictures.isEmpty(), ResultEnum.NOT_FOUND_ERROR, "图片不存在");
        //2、仅本人或管理员可删除
        for (PubPicture pubPicture : pubPictures) {
            ThrowUtils.throwIf(!pubPicture.getUserId().equals(StpUtil.getLoginIdAsLong()) || !StpUtil.hasRole(SystemRoleEnum.ADMIN.getValue()),
                    ResultEnum.NO_AUTH_ERROR);
        }
        int delete = pubPictureMapper.deleteByIds(ids);
        ThrowUtils.throwIf(delete <= 0, ResultEnum.OPERATION_ERROR, "图片删除失败");
        return pubPictures.stream().map(PubPicture::getThumbUrl).toList();
    }

    @Override
    public void edit(PictureEditDTO pictureEditDTO) {

        ThrowUtils.throwIf(pictureEditDTO == null || pictureEditDTO.getId() == null,
                ResultEnum.PARAMS_ERROR, "参数错误");

        // 1. 校验图片存在
        PubPicture pubPicture = pubPictureMapper.selectById(pictureEditDTO.getId());
        ThrowUtils.throwIf(pubPicture == null, ResultEnum.NOT_FOUND_ERROR, "图片不存在");
        // 2. 业务校验（分类 / 博客）
        if (picCategoryService != null && pictureEditDTO.getCategoryId() != null) {
            ThrowUtils.throwIf(picCategoryService.getById(pictureEditDTO.getCategoryId()) == null,
                    ResultEnum.PARAMS_ERROR, "图片分类不存在");
        }
        if (blogService != null && pictureEditDTO.getBlogId() != null) {
            ThrowUtils.throwIf(blogService.getById(pictureEditDTO.getBlogId()) == null,
                    ResultEnum.PARAMS_ERROR, "关联的博客不存在");
        }
        // 3. 权限校验：仅图片本人可编辑
        ThrowUtils.throwIf(!Objects.equals(pubPicture.getUserId(), StpUtil.getLoginIdAsLong()),
                ResultEnum.NO_AUTH_ERROR);
        // 4. 更新图片信息
        PubPicture updatePubPicture = new PubPicture();
        BeanUtils.copyProperties(pictureEditDTO, updatePubPicture);
        int update = pubPictureMapper.updateById(updatePubPicture);
        ThrowUtils.throwIf(update <= 0, ResultEnum.OPERATION_ERROR, "图片更新失败");

    }

    @Override
    public Page<PictureListVO> pageList(PictureQueryListDTO pictureQueryListDTO) {

        long current = pictureQueryListDTO.getCurrent();
        long pageSize = pictureQueryListDTO.getPageSize();

        PubPicture query = new PubPicture();
        BeanUtils.copyProperties(pictureQueryListDTO, query);
        QueryWrapper<PubPicture> qw = getQueryWrapper(query);
        if (StringUtils.isNotBlank(pictureQueryListDTO.getSearchText())) {
            qw.like("tags", pictureQueryListDTO.getSearchText())
                    .or()
                    .like("introduction", pictureQueryListDTO.getSearchText());
        }
        Integer pictureType = pictureQueryListDTO.getPictureType();
        if (pictureType != null) {
            if (pictureType.equals(0)) {
                // 横屏壁纸
                qw.ge("pic_scale", 1);
            } else if (pictureType.equals(1)) {
                // 竖屏壁纸
                qw.lt("pic_scale", 1);
            }
        }

        Page<PubPicture> page = pubPictureMapper.selectPage(new Page<>(current, pageSize), qw);
        List<PictureListVO> voList = page.getRecords().stream().map(p -> {
            PictureListVO vo = new PictureListVO();
            vo.setId(p.getId());
            vo.setThumbUrl(p.getThumbUrl());
            vo.setTags(p.getTags());
            vo.setPicScale(p.getPicScale());
            vo.setCollectCount(p.getCollectCount());
            vo.setIntroduction(p.getIntroduction());
            return vo;
        }).collect(Collectors.toList());
        Page<PictureListVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public QueryWrapper<PubPicture> getQueryWrapper(Object picture) {
        PubPicture pubPicture = (PubPicture) picture;

        Long id = pubPicture.getId();
        Long userId = pubPicture.getUserId();
        Long blogId = pubPicture.getBlogId();
        String introduction = pubPicture.getIntroduction();
        Long categoryId = pubPicture.getCategoryId();
        String tags = pubPicture.getTags();
        Long picSize = pubPicture.getPicSize();
        String picFormat = pubPicture.getPicFormat();
        Long collectCount = pubPicture.getCollectCount();
        Integer uploadStatus = pubPicture.getUploadStatus();
        Integer isRecommend = pubPicture.getIsRecommend();
        Integer reviewStatus = pubPicture.getReviewStatus();
        String reviewMessage = pubPicture.getReviewMessage();
        Long reviewerId = pubPicture.getReviewerId();
        Date reviewTime = pubPicture.getReviewTime();
        Date createTime = pubPicture.getCreateTime();
        Date updateTime = pubPicture.getUpdateTime();

        QueryWrapper<PubPicture> qw = new QueryWrapper<>();

        qw.eq(id != null, "id", id);
        qw.eq(userId != null, "user_id", userId);
        qw.eq(blogId != null, "blog_id", blogId);
        qw.like(StringUtils.isNoneBlank(introduction), "introduction", introduction);
        qw.eq(categoryId != null, "category_id", categoryId);
        qw.like(StringUtils.isNoneBlank(tags), "tags", tags);
        qw.ge(picSize != null, "pic_size", picSize);
        qw.eq(StringUtils.isNoneBlank(picFormat), "pic_format", picFormat);
        qw.orderBy(collectCount != null, false, "collect_count");
        qw.eq(uploadStatus != null, "upload_status", uploadStatus);
        qw.eq(isRecommend != null, "is_recommend", isRecommend);
        qw.eq(reviewStatus != null, "review_status", reviewStatus);
        qw.like(StringUtils.isNoneBlank(reviewMessage), "review_message", reviewMessage);
        qw.eq(reviewerId != null, "reviewer_id", reviewerId);
        qw.ge(reviewTime != null, "review_time", reviewTime);
        qw.ge(createTime != null, "create_time", createTime);
        qw.ge(updateTime != null, "update_time", updateTime);

        return qw;
    }

    @Override
    public PictureVO getOneById(Long id, Long spaceId) {
        PubPicture pubPicture = pubPictureMapper.selectById(id);
        ThrowUtils.throwIf(pubPicture == null, ResultEnum.NOT_FOUND_ERROR, "图片不存在");
        PictureVO pictureVO = new PictureVO();
        BeanUtils.copyProperties(pubPicture, pictureVO);
        return pictureVO;
    }

    @Override
    public String downLoad(PictureDownLoadDTO pictureDownLoadDTO) {
        pubPictureMapper.update(new UpdateWrapper<PubPicture>()
                .setSql("download_count = download_count + 1")
                .eq("id", pictureDownLoadDTO.getPictureId())
        );
        return pubPictureMapper.selectById(pictureDownLoadDTO.getPictureId()).getUrl();
    }
}
