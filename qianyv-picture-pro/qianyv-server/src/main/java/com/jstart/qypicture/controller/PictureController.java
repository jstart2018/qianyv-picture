package com.jstart.qypicture.controller;


import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jstart.qypicture.constant.RedisKey;
import com.jstart.qypicture.enums.ResultEnum;
import com.jstart.qypicture.model.dto.PictureDownLoadDTO;
import com.jstart.qypicture.model.dto.PictureEditDTO;
import com.jstart.qypicture.model.dto.PicturePageQueryDTO;
import com.jstart.qypicture.model.dto.PictureQueryListDTO;
import com.jstart.qypicture.model.entity.Blog;
import com.jstart.qypicture.model.vo.PictureListVO;
import com.jstart.qypicture.model.vo.PictureUploadVO;
import com.jstart.qypicture.model.vo.PictureVO;
import com.jstart.qypicture.result.Result;
import com.jstart.qypicture.service.PictureService;
import com.jstart.qypicture.utils.ThrowUtils;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
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
    @Resource
    private RedisTemplate redisTemplate;

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
     * 批量编辑图片
     */
    @PostMapping("/editByBatch")
    public Result<Boolean> editByBatch(@RequestBody List<PictureEditDTO> pictureEditDTOList) {
        ThrowUtils.throwIf(pictureEditDTOList == null || pictureEditDTOList.isEmpty(), ResultEnum.PARAMS_ERROR);
        pictureEditDTOList.forEach(this::edit);

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

    /**
     * 公共图片：图片分页查询（支持查询我发布的/我收藏的）
     */
    @PostMapping("/list/page")
    public Result<Page<PictureListVO>> listByPage(@RequestBody PicturePageQueryDTO picturePageQueryDTO) {
        ThrowUtils.throwIf(picturePageQueryDTO == null, ResultEnum.PARAMS_ERROR, "请求参数为空");

        Page<PictureListVO> page = pictureService.selectPictureByPage(picturePageQueryDTO);

        return Result.success(page);
    }

    /**
     * 获取图片详情
     */
    @GetMapping("/{id}")
    public Result<PictureVO> getOneById(@PathVariable("id") Long id,@RequestParam(required = false) Long spaceId) {

        PictureVO pictureVO = pictureService.getOneById(id,spaceId);

        return Result.success(pictureVO);
    }

    /**
     * 下载图片
     */
    @PostMapping("/download")
    public Result<String> pictureDownload(@RequestBody PictureDownLoadDTO pictureDownLoadDTO) {

        StpUtil.getLoginIdAsLong();
        String url = pictureService.downLoad(pictureDownLoadDTO);

        return Result.success(url);
    }

    /**
     * 图片收藏/取消收藏
     * @param id 图片 id
     * @return 是否操作成功
     */
    @PostMapping("/{id}")
    public Result collectToggle(@PathVariable("id") Long id) {
        ThrowUtils.throwIf(id == null , ResultEnum.PARAMS_ERROR);
        pictureService.collectToggle(id);
        return Result.success();
    }

    /**
     * 查看用户是否收藏该图片
     */
    @GetMapping("/checkCollect")
    public Result<Boolean> checkCollect(@RequestParam Long id) {
        ThrowUtils.throwIf(id == null, ResultEnum.PARAMS_ERROR, "参数错误");
        PictureVO pictureVO = pictureService.getOneById(id, null);
        ThrowUtils.throwIf(pictureVO == null, ResultEnum.NOT_FOUND_ERROR, "图片不存在");
        try {
            Boolean isMember = redisTemplate.opsForSet().isMember(RedisKey.PICTURE_COLLECTION_KEY + id, StpUtil.getLoginIdAsLong());
            return Result.success(isMember);
        } catch (NotLoginException e) {
            return Result.success(Boolean.FALSE);
        }
    }


    //统计图片数
    @GetMapping("/count")
    public Result<Long> count() {
        Long count = pictureService.count();
        return Result.success(count);
    }

}
