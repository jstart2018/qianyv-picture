package com.jstart.qypicture.controller;


import com.jstart.qypicture.enums.ResultEnum;
import com.jstart.qypicture.model.dto.PicCategoryDTO;
import com.jstart.qypicture.model.entity.PicCategory;
import com.jstart.qypicture.model.vo.PicCategoryVO;
import com.jstart.qypicture.result.Result;
import com.jstart.qypicture.service.PicCategoryService;
import com.jstart.qypicture.utils.ThrowUtils;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/picCategory/")
public class PicCategoryController {

    @Resource
    private PicCategoryService picCategoryService;

    /**
     * 添加图片分类
     */
    @PostMapping("add")
    public Result<PicCategoryVO> add(@RequestBody PicCategoryDTO picCategoryDTO) {

        boolean exists = picCategoryService.lambdaQuery()
                .eq(picCategoryDTO.getCategoryName() != null, PicCategory::getCategoryName, picCategoryDTO.getCategoryName())
                .exists();
        ThrowUtils.throwIf(exists, ResultEnum.OPERATION_ERROR, "分类已存在");

        PicCategory pc = new PicCategory();
        BeanUtils.copyProperties(picCategoryDTO, pc);
        pc.setId(null);
        boolean save = picCategoryService.save(pc);
        ThrowUtils.throwIf(!save, ResultEnum.OPERATION_ERROR, "添加失败");

        PicCategoryVO picCategoryVO = new PicCategoryVO();
        BeanUtils.copyProperties(pc, picCategoryVO);

        return Result.success(picCategoryVO);
    }

    /**
     * 删除图片分类
     */
    @PostMapping("delete")
    public Result<?> delete(@RequestBody PicCategoryDTO picCategoryDTO) {

        boolean exists = picCategoryService.lambdaQuery()
                .eq(picCategoryDTO.getCategoryName() != null, PicCategory::getCategoryName, picCategoryDTO.getCategoryName())
                .exists();
        ThrowUtils.throwIf(!exists, ResultEnum.OPERATION_ERROR, "分类不存在");

        boolean remove = picCategoryService.lambdaUpdate()
                .eq(PicCategory::getCategoryName, picCategoryDTO.getCategoryName())
                .remove();
        ThrowUtils.throwIf(!remove, ResultEnum.OPERATION_ERROR, "删除失败");

        return Result.success();
    }

    /**
     * 获取所有图片分类
     */
    @GetMapping("listAll")
    public Result<List<PicCategoryVO>> listAll() {
        List<PicCategory> list = picCategoryService.lambdaQuery()
                .select(PicCategory::getId,PicCategory::getCategoryName)
                .list();
        List<PicCategoryVO> picCategoryVOList = list.stream().map(picCategory -> {
            PicCategoryVO picCategoryVO = new PicCategoryVO();
            BeanUtils.copyProperties(picCategory, picCategoryVO);
            return picCategoryVO;
        }).toList();

        return Result.success(picCategoryVOList);
    }


}
