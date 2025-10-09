package com.jstart.qypicture.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jstart.qypicture.enums.PicturePlaceEnum;
import com.jstart.qypicture.enums.ResultEnum;
import com.jstart.qypicture.handler.picture.PictureHandler;
import com.jstart.qypicture.handler.picture.PictureHandlerFactory;
import com.jstart.qypicture.mapper.PubPictureMapper;
import com.jstart.qypicture.model.dto.PictureEditDTO;
import com.jstart.qypicture.model.dto.PictureQueryListDTO;
import com.jstart.qypicture.model.vo.PictureListVO;
import com.jstart.qypicture.model.vo.PictureUploadVO;
import com.jstart.qypicture.service.PictureService;
import com.jstart.qypicture.utils.COSUtil.CosManager;
import com.jstart.qypicture.utils.ThrowUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

// todo 每次用switch分支判断公共和空间，考虑策略模式优化
@Service
@Slf4j
public class PictureServiceImpl implements PictureService {

    @Resource
    private PictureHandlerFactory pictureHandlerFactory;

    @Resource
    private CosManager cosManager;

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
     * @param id 图片 id
     * @return 是否删除成功
     */
    @Override
    public boolean delete(Long id, Long spaceId) {
        //1.校验权限
        PicturePlaceEnum manageType = PicturePlaceEnum.getManageType(spaceId);
        //2.从工厂获取策略
        PictureHandler<?> pictureHandler = pictureHandlerFactory.getPictureSpaceHandler(manageType);
        //3.策略模式执行删除
        String thumbUrl = pictureHandler.delete(id, spaceId);
        //4.删除 云服务文件（只删除缩略图）
        cosManager.deleteObject(thumbUrl);
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

}