package com.jstart.qypicture.handler.picture;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jstart.qypicture.enums.PicturePlaceEnum;
import com.jstart.qypicture.model.dto.PictureEditDTO;
import com.jstart.qypicture.model.dto.PictureQueryListDTO;
import com.jstart.qypicture.model.vo.PictureListVO;
import com.jstart.qypicture.model.vo.PictureUploadVO;

public interface PictureHandler<T> {

    /**
     * 获取图片存储位置枚举
     * @return
     */
    PicturePlaceEnum getPicturePlaceEnum();

    /**
     * 上传图片
     * @param inputSource 输入源，可以是文件路径、文件
     * @param spaceId 空间ID，如果是公共空间则为null
     * @return 上传结果VO
     */
    PictureUploadVO upload(Object inputSource, Long spaceId);

    /**
     * 删除图片
     *
     * @param id      图片ID
     * @param spaceId 空间ID，如果是公共空间则为null
     * @return 返回缩略图url，给外部删除云服务中的文件
     */
    String delete(Long id, Long spaceId);

    /**
     * 编辑图片信息
     * @param pictureEditDTO 编辑参数DTO
     */
    void edit(PictureEditDTO pictureEditDTO);

    /**
     * 分页列出图片
     * @param pictureQueryListDTO 查询参数DTO
     * @return 分页结果VO
     */
    Page<PictureListVO> pageList(PictureQueryListDTO pictureQueryListDTO);

    /**
     * 获取查询包装器
     * @param picture 查询参数对象
     * @return 查询包装器
     */
    QueryWrapper<T> getQueryWrapper(Object picture);

}
