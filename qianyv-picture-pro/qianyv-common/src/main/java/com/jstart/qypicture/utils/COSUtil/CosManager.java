package com.jstart.qypicture.utils.COSUtil;

import com.jstart.qypicture.enums.ResultEnum;
import com.jstart.qypicture.exception.BusinessException;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.GetObjectRequest;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.PicOperations;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Component
public class CosManager {

    @Resource
    private CosClientConfig cosClientConfig;

    @Resource
    private COSClient cosClient;

    // ... 一些操作 COS 的方法

    /**
     * 上传对象
     *
     * @param key  存储桶的唯一路径，可以包含目录，例如：folder1/folder2/file.txt
     * @param file 文件
     */
    public PutObjectResult putObject(String key, File file) {
        PutObjectRequest putObjectRequest =
                new PutObjectRequest(cosClientConfig.getBucket(), key, file);
        return cosClient.putObject(putObjectRequest);
    }

    /**
     * 下载对象
     *
     * @param key 唯一路径
     */
    public COSObject getObject(String key) {
        GetObjectRequest getObjectRequest = new GetObjectRequest(cosClientConfig.getBucket(), key);
        return cosClient.getObject(getObjectRequest);
    }

    /**
     * 专门上传图片的
     * 1.返回原图信息
     * 2.增加了缩略图规则
     *
     * @param key  唯一键
     * @param file 文件
     */
    public PutObjectResult putPictureObject(String key, File file) {
        //构建上传请求
        PutObjectRequest putObjectRequest =
                new PutObjectRequest(cosClientConfig.getBucket(), key, file);
        //是否返回图像信息
        PicOperations picOperations = new PicOperations();
        // 表示返回原图信息
        picOperations.setIsPicInfo(1);
        if (file.length() > 1024 * 2) {
            List<PicOperations.Rule> rules = new ArrayList<>();
            // 缩略图处理
            PicOperations.Rule thumbnailRule = new PicOperations.Rule();
            thumbnailRule.setBucket(cosClientConfig.getBucket());
            String fileMainName = key.substring(0, key.lastIndexOf(".")); //获取文件主名称
            String fileSuffix = key.substring(key.lastIndexOf(".") + 1); //获取文件后缀
            //缩略图路径，uuid比原路径少4位，并且加上 _thumb
            String thumbnailKey = fileMainName.substring(0, fileMainName.length() - 4) + "_t." + fileSuffix;


            thumbnailRule.setFileId(thumbnailKey);
            // 缩放规则 /thumbnail/<Width>x<Height>>（如果大于原图宽高，则不处理）
            thumbnailRule.setRule(String.format("imageMogr2/thumbnail/%sx%s>", 512, 512));
            rules.add(thumbnailRule);
            // 构造处理参数
            picOperations.setRules(rules);
        }
        putObjectRequest.setPicOperations(picOperations);

        return cosClient.putObject(putObjectRequest);
    }


    /**
     * 删除对象
     *
     * @param key 唯一键
     */
    public void deleteObject(String key) {
        try {
            cosClient.deleteObject(cosClientConfig.getBucket(), key);
        } catch (Exception e) {
            throw new BusinessException(ResultEnum.OPERATION_ERROR, "COS中删除对象异常");
        }
    }
}