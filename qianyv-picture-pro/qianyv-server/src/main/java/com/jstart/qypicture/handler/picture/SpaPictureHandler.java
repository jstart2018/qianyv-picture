package com.jstart.qypicture.handler.picture;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jstart.qypicture.auth.SpaceRoleEnum;
import com.jstart.qypicture.enums.PicturePlaceEnum;
import com.jstart.qypicture.enums.ResultEnum;
import com.jstart.qypicture.exception.BusinessException;
import com.jstart.qypicture.mapper.SpaPictureMapper;
import com.jstart.qypicture.model.UploadPictureResult;
import com.jstart.qypicture.model.dto.PictureEditDTO;
import com.jstart.qypicture.model.dto.PictureQueryListDTO;
import com.jstart.qypicture.model.entity.SpaPicture;
import com.jstart.qypicture.model.entity.Space;
import com.jstart.qypicture.model.vo.PictureListVO;
import com.jstart.qypicture.model.vo.PictureUploadVO;
import com.jstart.qypicture.service.SpaceService;
import com.jstart.qypicture.template.uploadFileTemplate.PictureUploadTemplate;
import com.jstart.qypicture.utils.ThrowUtils;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class SpaPictureHandler implements PictureHandler<SpaPicture> {

    @Resource
    private PictureUploadTemplate filePictureUpload;
    @Resource
    private PictureUploadTemplate urlPictureUpload;
    @Resource
    private SpaPictureMapper spaPictureMapper;
    @Resource
    private SpaceService spaceService;
    @Resource
    private TransactionTemplate transactionTemplate;



    @Override
    public PicturePlaceEnum getPicturePlaceEnum() {
        return PicturePlaceEnum.SPACE;
    }

    @Override
    public PictureUploadVO upload(Object inputSource, Long spaceId) {

        Space space = validateSpaceAuth(spaceId, SpaceRoleEnum.EDITOR);
        //校验空间额度
        if (space.getTotalCount() >= space.getMaxCount()) {
            throw new BusinessException(ResultEnum.OPERATION_ERROR, "空间余额不足");
        }
        if (space.getTotalSize() >= space.getMaxSize()) {
            throw new BusinessException(ResultEnum.OPERATION_ERROR, "空间余额不足");
        }

        String uploadPathPrefix = String.format("space/%s", spaceId);

        // 根据 inputSource 的类型区分上传方式
        PictureUploadTemplate pictureUploadTemplate = filePictureUpload;
        if (inputSource instanceof String) {
            pictureUploadTemplate = urlPictureUpload;
        }
        UploadPictureResult uploadPictureResult = pictureUploadTemplate.uploadPicture(inputSource, uploadPathPrefix);

        SpaPicture spaPicture = new SpaPicture();
        spaPicture.setUserId(StpUtil.getLoginIdAsLong());
        //todo set目录id
        //应该也setUploadStatus，但是数据库有默认值
        BeanUtils.copyProperties(uploadPictureResult, spaPicture);

        return transactionTemplate.execute(status -> {
            //1.插入图片
            spaPicture.setSpaceId(spaceId);
            //todo 空间内图片塞入默认目录（创建空间时创建一个默认目录）
            spaPicture.setCatalogId(0L);
            int insert = spaPictureMapper.insert(spaPicture);
            ThrowUtils.throwIf(insert <= 0, ResultEnum.OPERATION_ERROR, "图片上传数据库失败");

            //2.更新空间信息
            boolean updateResult = spaceService.lambdaUpdate().eq(Space::getId, spaceId)
                    .setSql("total_size = total_size+" + spaPicture.getPicSize())
                    .setSql("total_count = total_count+ 1")
                    .update();
            ThrowUtils.throwIf(!updateResult, ResultEnum.OPERATION_ERROR, "空间更新失败");

            PictureUploadVO pictureUploadVO = new PictureUploadVO();
            pictureUploadVO.setId(spaPicture.getId());
            pictureUploadVO.setThumbUrl(spaPicture.getThumbUrl());

            return pictureUploadVO;
        });

    }

    @Override
    public String delete(Long id, Long spaceId) {
        //1.校验图片是否存在
        SpaPicture spaPicture = spaPictureMapper.selectById(id);
        ThrowUtils.throwIf(spaPicture == null, ResultEnum.NOT_FOUND_ERROR, "图片不存在");
        //2.校验空间权限
        validateSpaceAuth(spaceId, SpaceRoleEnum.EDITOR);

        //3.删除图片
        int delete = spaPictureMapper.deleteById(id);
        ThrowUtils.throwIf(delete <= 0, ResultEnum.OPERATION_ERROR, "图片删除失败");

        return spaPicture.getThumbUrl();
    }

    @Override
    public void edit(PictureEditDTO pictureEditDTO) {
        ThrowUtils.throwIf(pictureEditDTO == null || pictureEditDTO.getId() == null,
                ResultEnum.PARAMS_ERROR, "参数错误");

        Long spaceId = pictureEditDTO.getSpaceId();
        // 1. 空间权限校验
        validateSpaceAuth(spaceId, SpaceRoleEnum.EDITOR);
        // 2. 校验图片存在且归属该空间
        SpaPicture dbSpaPicture = spaPictureMapper.selectById(pictureEditDTO.getId());
        ThrowUtils.throwIf(dbSpaPicture == null, ResultEnum.NOT_FOUND_ERROR, "图片不存在");
        ThrowUtils.throwIf(!Objects.equals(dbSpaPicture.getSpaceId(), spaceId),
                ResultEnum.NO_AUTH_ERROR, "无权编辑该空间图片");
        // 3. 仅更新允许编辑的字段（简介、标签等）
        SpaPicture updateSpaPicture = new SpaPicture();
        updateSpaPicture.setId(pictureEditDTO.getId());
        updateSpaPicture.setSpaceId(spaceId);
        updateSpaPicture.setIntroduction(pictureEditDTO.getIntroduction());
        updateSpaPicture.setTags(pictureEditDTO.getTags());
        int update = spaPictureMapper.updateById(updateSpaPicture);
        ThrowUtils.throwIf(update <= 0, ResultEnum.OPERATION_ERROR, "图片更新失败");

    }

    @Override
    public Page<PictureListVO> pageList(PictureQueryListDTO pictureQueryListDTO) {

        validateSpaceAuth(pictureQueryListDTO.getSpaceId(), SpaceRoleEnum.VIEWER);

        long current = pictureQueryListDTO.getCurrent();
        long pageSize = pictureQueryListDTO.getPageSize();

        SpaPicture query = new SpaPicture();
        BeanUtils.copyProperties(pictureQueryListDTO, query);
        query.setSpaceId(pictureQueryListDTO.getSpaceId());
        QueryWrapper<SpaPicture> qw = getQueryWrapper(query);
        Page<SpaPicture> page = spaPictureMapper.selectPage(new Page<>(current, pageSize), qw);
        List<PictureListVO> voList = page.getRecords().stream().map(p -> {
            PictureListVO vo = new PictureListVO();
            vo.setId(p.getId());
            vo.setThumbUrl(p.getThumbUrl());
            vo.setTags(p.getTags());
            return vo;
        }).collect(Collectors.toList());
        Page<PictureListVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        voPage.setRecords(voList);

        return voPage;
    }

    @Override
    public QueryWrapper<SpaPicture> getQueryWrapper(Object picture) {
        SpaPicture spaPicture = (SpaPicture) picture;

        Long id = spaPicture.getId();
        Long userId = spaPicture.getUserId();
        Long spaceId = spaPicture.getSpaceId();
        Long catalogId = spaPicture.getCatalogId();
        String introduction = spaPicture.getIntroduction();
        String tags = spaPicture.getTags();
        Long picSize = spaPicture.getPicSize();
        String picFormat = spaPicture.getPicFormat();
        Integer uploadStatus = spaPicture.getUploadStatus();
        Integer reviewStatus = spaPicture.getReviewStatus();
        String reviewMessage = spaPicture.getReviewMessage();
        Long reviewerId = spaPicture.getReviewerId();
        Date reviewTime = spaPicture.getReviewTime();
        Date createTime = spaPicture.getCreateTime();
        Date updateTime = spaPicture.getUpdateTime();
        Date deleteTime = spaPicture.getDeleteTime();

        QueryWrapper<SpaPicture> qw = new QueryWrapper<>();

        qw.eq(id != null, "id", id);
        qw.eq(userId != null, "user_id", userId);
        qw.eq(spaceId != null, "space_id", spaceId);
        qw.eq(catalogId != null, "catalog_id", catalogId);
        qw.like(StringUtils.isNoneBlank(introduction), "introduction", introduction);
        qw.like(StringUtils.isNoneBlank(tags), "tags", tags);
        qw.ge(picSize != null, "pic_size", picSize);
        qw.eq(StringUtils.isNoneBlank(picFormat), "pic_format", picFormat);
        qw.eq(uploadStatus != null, "upload_status", uploadStatus);
        qw.eq(reviewStatus != null, "review_status", reviewStatus);
        qw.like(StringUtils.isNoneBlank(reviewMessage), "review_message", reviewMessage);
        qw.eq(reviewerId != null, "reviewer_id", reviewerId);
        qw.ge(reviewTime != null, "review_time", reviewTime);
        qw.ge(createTime != null, "create_time", createTime);
        qw.ge(updateTime != null, "update_time", updateTime);
        qw.ge(deleteTime != null, "delete_time", deleteTime);

        return qw;
    }

    //校验空间属性、成员权限
    private Space validateSpaceAuth(Long spaceId, SpaceRoleEnum reqRole) {
        Space space = spaceService.getById(spaceId);
        //（1）校验空间是否存在
        ThrowUtils.throwIf(space == null, ResultEnum.NOT_FOUND_ERROR, "空间不存在");

        //todo 校验空间成员的权限 后续开启sa-token多账号权限认证
        switch (reqRole) {
            case CREATOR -> {

            }
            case ADMIN -> {
            }
            case EDITOR -> {
            }
            case VIEWER -> {
            }
        }

        return space;

    }


}
