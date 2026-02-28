package com.jstart.qypicture.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jstart.qypicture.constant.RedisKey;
import com.jstart.qypicture.enums.CollectionEnum;
import com.jstart.qypicture.enums.PicturePlaceEnum;
import com.jstart.qypicture.enums.PictureStatusEnum;
import com.jstart.qypicture.enums.ResultEnum;
import com.jstart.qypicture.handler.picture.PictureHandler;
import com.jstart.qypicture.handler.picture.PictureHandlerFactory;
import com.jstart.qypicture.mapper.PubPictureMapper;
import com.jstart.qypicture.mapper.SpaPictureMapper;
import com.jstart.qypicture.model.dto.PictureDownLoadDTO;
import com.jstart.qypicture.model.dto.PictureEditDTO;
import com.jstart.qypicture.model.dto.PicturePageQueryDTO;
import com.jstart.qypicture.model.dto.PictureQueryListDTO;
import com.jstart.qypicture.model.entity.PubPicture;
import com.jstart.qypicture.model.vo.PictureListVO;
import com.jstart.qypicture.model.vo.PictureUploadVO;
import com.jstart.qypicture.model.vo.PictureVO;
import com.jstart.qypicture.model.vo.UserInfoVO;
import com.jstart.qypicture.service.CollectionService;
import com.jstart.qypicture.service.PictureService;
import com.jstart.qypicture.service.UserService;
import com.jstart.qypicture.utils.COSUtil.CosManager;
import com.jstart.qypicture.utils.ThrowUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

// todo 每次用switch分支判断公共和空间，考虑策略模式优化
@Service
@Slf4j
public class PictureServiceImpl implements PictureService {

    @Resource
    @Lazy
    private PictureHandlerFactory pictureHandlerFactory;
    @Resource
    private CosManager cosManager;
    @Resource
    private UserService userService;
    @Resource
    private PubPictureMapper pubPictureMapper;
    @Resource
    @Lazy
    private CollectionService collectionService;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private SpaPictureMapper spaPictureMapper;

    /**
     * 上传图片
     *
     * @param inputSource 输入源（可能是 MultipartFile、URL等）
     * @param spaceId     空间 id（可选）为null时上传到公共空间
     * @return
     */
    @Override
    public PictureUploadVO upload(Object inputSource, Long spaceId) {

        PicturePlaceEnum manageType = PicturePlaceEnum.getManageType(spaceId);
        //从工厂获取策略
        PictureHandler<?> pictureSpaceHandler = pictureHandlerFactory.getPictureSpaceHandler(manageType);
        //策略模式执行上传
        return pictureSpaceHandler.upload(inputSource, spaceId);
    }

    /**
     * 删除图片
     *
     * @param ids 图片 id
     * @return 是否删除成功
     */
    @Override
    public boolean delete(List<Long> ids, Long spaceId) {
        //1.校验权限
        PicturePlaceEnum manageType = PicturePlaceEnum.getManageType(spaceId);
        //2.从工厂获取策略
        PictureHandler<?> pictureHandler = pictureHandlerFactory.getPictureSpaceHandler(manageType);
        //3.策略模式执行删除
        List<String> thumbUrls = pictureHandler.delete(ids, spaceId);
        //4.删除 云服务文件（只删除缩略图）
        thumbUrls.forEach(thumbUrl -> cosManager.deleteObject(thumbUrl));
        return true;
    }


    /**
     * 编辑图片信息（公共图库或空间图库）
     *
     * @param pictureEditDTO 参数（spaceId 为空编辑公共图库；非空编辑空间图库）
     */
    @Override
    public void edit(PictureEditDTO pictureEditDTO) {
        ThrowUtils.throwIf(pictureEditDTO == null || pictureEditDTO.getId() == null,
                ResultEnum.PARAMS_ERROR, "参数错误");

        PicturePlaceEnum manageType = PicturePlaceEnum.getManageType(pictureEditDTO.getSpaceId());
        //从工厂获取策略
        PictureHandler<?> pictureSpaceHandler = pictureHandlerFactory.getPictureSpaceHandler(manageType);
        //策略模式执行修改
        pictureSpaceHandler.edit(pictureEditDTO);
    }

    /**
     * 分页查询图片
     *
     * @param pictureQueryListDTO 查询条件（含 spaceId：为空则查公共图库；不为空则查空间图库）
     */
    @Override
    public Page<PictureListVO> pageList(PictureQueryListDTO pictureQueryListDTO) {

        PicturePlaceEnum manageType = PicturePlaceEnum.getManageType(pictureQueryListDTO.getSpaceId());
        //从工厂获取策略
        PictureHandler<?> pictureSpaceHandler = pictureHandlerFactory.getPictureSpaceHandler(manageType);
        //策略模式执行查询
        return pictureSpaceHandler.pageList(pictureQueryListDTO);
    }

    @Override
    public PictureVO getOneById(Long id, Long spaceId) {

        PicturePlaceEnum manageType = PicturePlaceEnum.getManageType(spaceId);
        //从工厂获取策略
        PictureHandler<?> pictureSpaceHandler = pictureHandlerFactory.getPictureSpaceHandler(manageType);
        //策略模式执行查询
        PictureVO pictureVO = pictureSpaceHandler.getOneById(id, spaceId);
        UserInfoVO userInfoVO = userService.getUserInfoVO(userService.getById(pictureVO.getUserId()));
        pictureVO.setUserInfoVO(userInfoVO);

        return pictureVO;

    }

    @Override
    public String downLoad(PictureDownLoadDTO pictureDownLoadDTO) {

        PicturePlaceEnum manageType = PicturePlaceEnum.getManageType(pictureDownLoadDTO.getSpaceId());
        //从工厂获取策略
        PictureHandler<?> pictureSpaceHandler = pictureHandlerFactory.getPictureSpaceHandler(manageType);
        //策略模式执行下载
        return pictureSpaceHandler.downLoad(pictureDownLoadDTO);
    }

    @Override
    public void collectToggle(Long id) {
        PubPicture pubPicture = pubPictureMapper.selectById(id);
        ThrowUtils.throwIf(pubPicture == null, ResultEnum.NOT_FOUND_ERROR, "图片不存在");

        collectionService.collectionToggle(StpUtil.getLoginIdAsLong(),id, CollectionEnum.PICTURE);

        //pubPictureMapper.updateCollectCount(id,1L);

    }

    /**
     * 公共图库：图片分页查询（支持查询我发布的/我收藏的）
     * @param dto
     * @return
     */
    @Override
    public Page<PictureListVO> selectPictureByPage(PicturePageQueryDTO dto) {
        Long userId = dto.getUserId();
        Boolean myCollect = dto.getMyCollect();
        String searchText = dto.getSearchText();
        Long categoryId = dto.getCategoryId();
        Integer pictureType = dto.getPictureType();
        Integer isRecommend = dto.getIsRecommend();
        Integer reviewStatus = PictureStatusEnum.PASS.getValue();

        QueryWrapper<PubPicture> qw = new QueryWrapper<>();
        List<Long> pictureIds = null;

        // 查询我收藏的图片
        if (BooleanUtils.isTrue(myCollect)) {
            long loginUserId = StpUtil.getLoginIdAsLong();
            Set<Object> collectedPictureIds = redisTemplate.opsForSet().members(RedisKey.USER_PICTURE_COLLECTION_KEY + loginUserId);
            if (collectedPictureIds == null || collectedPictureIds.isEmpty()) {
                Page<PictureListVO> emptyPage = new Page<>(dto.getCurrent(), dto.getPageSize(), 0);
                emptyPage.setRecords(Collections.emptyList());
                return emptyPage;
            }
            pictureIds = collectedPictureIds.stream()
                    .map(obj -> Long.valueOf(obj.toString()))
                    .collect(Collectors.toList());
        }

        // 如果有指定图片ID列表，添加in条件
        if (pictureIds != null && !pictureIds.isEmpty()) {
            qw.in("id", pictureIds);
        }

        // 查询我发布的图片
        qw.eq(userId != null, "user_id", userId);

        // 搜索条件
        if (StrUtil.isNotBlank(searchText)) {
            qw.and(wrapper -> wrapper
                    .like("tags", searchText)
                    .or()
                    .like("introduction", searchText)
            );
        }

        qw.eq(categoryId != null, "category_id", categoryId);
        qw.eq(isRecommend != null, "is_recommend", isRecommend);
        qw.eq(reviewStatus != null, "review_status", reviewStatus);

        // 图片类型筛选
        if (pictureType != null) {
            if (pictureType.equals(0)) {
                // 横屏壁纸
                qw.ge("pic_scale", 1);
            } else if (pictureType.equals(1)) {
                // 竖屏壁纸
                qw.lt("pic_scale", 1);
            }
        }

        qw.orderBy(true, false, "id");

        // 分页查询
        Page<PubPicture> page = pubPictureMapper.selectPage(new Page<>(dto.getCurrent(), dto.getPageSize()), qw);
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
    public Long count() {
        Long pubCount = pubPictureMapper.selectCount(null);
        Long spaCount = spaPictureMapper.selectCount(null);
        return pubCount + spaCount;
    }

}