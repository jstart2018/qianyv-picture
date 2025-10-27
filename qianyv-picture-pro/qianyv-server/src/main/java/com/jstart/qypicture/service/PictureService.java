package com.jstart.qypicture.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jstart.qypicture.model.dto.PictureEditDTO;
import com.jstart.qypicture.model.dto.PictureQueryListDTO;
import com.jstart.qypicture.model.vo.PictureListVO;
import com.jstart.qypicture.model.vo.PictureUploadVO;

import java.util.List;

public interface PictureService {

    /**
     * 上传图片
     *
     * @param inputSource 输入源（可能是 MultipartFile、URL等）
     * @param spaceId     空间 id（可选）为null时上传到公共空间
     * @return 图片访问 url
     */
    PictureUploadVO upload(Object inputSource, Long spaceId);

    /**
     * 删除图片
     *
     * @param ids 图片 id
     * @return 是否删除成功
     */
    boolean delete(List<Long> ids, Long spaceId);

    /**
     * 编辑图片信息
     *
     * @param pictureEditDTO 参数
     */
    void edit(PictureEditDTO pictureEditDTO);

    /**
     * 分页列出图片
     * <p>
     * 根据 spaceId 判断查询公共图库或空间图库
     */
    Page<PictureListVO> pageList(PictureQueryListDTO pictureQueryListDTO);


}
