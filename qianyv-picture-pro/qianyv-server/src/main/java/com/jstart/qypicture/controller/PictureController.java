package com.jstart.qypicture.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jstart.qypicture.enums.ResultEnum;
import com.jstart.qypicture.model.dto.PictureEditDTO;
import com.jstart.qypicture.model.dto.PictureQueryListDTO;
import com.jstart.qypicture.model.vo.PictureListVO;
import com.jstart.qypicture.model.vo.PictureUploadVO;
import com.jstart.qypicture.result.Result;
import com.jstart.qypicture.service.PictureService;
import com.jstart.qypicture.utils.ThrowUtils;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collection;
import java.util.List;


/**
 * 图片接口
 *
 * @author 28435
 */
@RestController
@RequestMapping("/picture")
public class PictureController {

    @Resource
    private PictureService pictureService;

    /**
     * 上传本地图片
     *
     * @param file    图片文件
     * @param spaceId 空间 id（可选）为null时上传到公共空间
     * @return 图片访问 url
     */
    @PostMapping("/uploadByFile")
    public Result<PictureUploadVO> upload(@RequestPart("file") MultipartFile file, @RequestParam(required = false) Long spaceId) {

        PictureUploadVO pictureUploadVO = pictureService.upload(file, spaceId);

        return Result.success(pictureUploadVO);
    }

    /**
     * 上传网络图片
     *
     * @param url     图片 url
     * @param spaceId 空间 id（可选）为null时上传到公共空间
     * @return 图片访问 url
     */
    @PostMapping("/uploadByUrl")
    public Result<PictureUploadVO> uploadByUrl(String url, @RequestParam(required = false) Long spaceId) {

        PictureUploadVO pictureUploadVO = pictureService.upload(url, spaceId);

        return Result.success(pictureUploadVO);
    }

    /**
     * 删除图片
     *
     * @param id 图片 id
     * @return 是否删除成功
     */
    @DeleteMapping("/delete")
    public Result<Boolean> delete(Long id, @RequestParam(required = false) Long spaceId) {
        boolean delete = pictureService.delete(List.of(id), spaceId);
        return Result.success(delete);
    }

    /**
     * 编辑图片
     */
    @PostMapping("/edit")
    public Result<Boolean> edit(@RequestBody PictureEditDTO pictureEditDTO) {
        ThrowUtils.throwIf(pictureEditDTO == null || pictureEditDTO.getId() == null, ResultEnum.PARAMS_ERROR);
        ThrowUtils.throwIf(pictureEditDTO.getBlogId() != null, ResultEnum.PARAMS_ERROR);

        pictureService.edit(pictureEditDTO);

        return Result.success(true);
    }

    /**
     * 图片列表（分页）
     */
    @PostMapping("/list")
    public Result<Page<PictureListVO>> list(@RequestBody PictureQueryListDTO pictureQueryListDTO) {

        Page<PictureListVO> page = pictureService.pageList(pictureQueryListDTO);

        return Result.success(page);
    }


}
