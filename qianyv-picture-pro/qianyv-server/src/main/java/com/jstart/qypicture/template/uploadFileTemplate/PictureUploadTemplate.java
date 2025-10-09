package com.jstart.qypicture.template.uploadFileTemplate;


import com.jstart.qypicture.enums.ResultEnum;
import com.jstart.qypicture.exception.BusinessException;
import com.jstart.qypicture.model.UploadPictureResult;
import com.jstart.qypicture.utils.COSUtil.CosClientConfig;
import com.jstart.qypicture.utils.COSUtil.CosManager;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.CIObject;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import com.qcloud.cos.model.ciModel.persistence.ProcessResults;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Slf4j
public abstract class PictureUploadTemplate {

    @Resource
    private CosClientConfig cosClientConfig;

    @Resource
    private CosManager cosManager;

    // ...

    /**
     * 上传文件
     *
     * @param inputSource 文件来源
     * @param directory   上传目录前缀
     * @return 上传后获取到的原图片信息
     */
    public final UploadPictureResult uploadPicture(Object inputSource, String directory) {
        //1、校验图片
        checkPictureObject(inputSource);


        //2、获取原始文件名
        String originalFilename = getOriginalFilename(inputSource);
        String pictureSuffix = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
        //文件后缀长度过长时，强制设置为webp
        if (pictureSuffix.length() > 4) {
            pictureSuffix = "webp";
        }
        //3、构建新文件名字
        String fileName = String.format("%s_%s.%s",
                LocalDate.now(),
                UUID.randomUUID().toString().replaceAll("-", "").substring(0, 16),
                pictureSuffix);

        //4、构建文件上传路径
        String key = String.format("%s/%s", directory, fileName);
        File file = null;
        try {
            //5、构建临时文件
            file = File.createTempFile(key, null);
            //6、填充上传文件
            processFile(inputSource, file);
            //7、上传文件
            PutObjectResult putObjectResult = cosManager.putPictureObject(key, file);
            //8、解析返回结果
            //主图信息
            ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();
            //缩略图信息
            ProcessResults processResults = putObjectResult.getCiUploadResult().getProcessResults();
            List<CIObject> objectList = processResults.getObjectList();
            CIObject thumbnailCiObject = null;
            if (objectList != null && !objectList.isEmpty()) {
                thumbnailCiObject = objectList.get(0);
            }
            return getUploadPictureResult(imageInfo, key,
                    originalFilename, file, thumbnailCiObject);

        } catch (IOException e) {
            log.error(e.getMessage());
            throw new BusinessException(ResultEnum.OPERATION_ERROR, "上传文件失败");
        } finally {
            //9、删除文件
            if (file != null){
                boolean delete = file.delete();
                if (!delete) {
                    log.error("file delete error, filepath = {}", file.getAbsoluteFile());//打印绝对路径
                }
            }
        }

    }

    /**
     * 处理上传图片后的返回结果
     *
     * @param imageInfo
     * @param originalFilename
     * @param file
     * @return
     */
    private UploadPictureResult getUploadPictureResult(ImageInfo imageInfo,
                                                       String key,
                                                       String originalFilename,
                                                       File file,
                                                       CIObject thumbnailCiObject) {
        int picWidth = imageInfo.getWidth();
        int picHeight = imageInfo.getHeight();
        //计算图片宽高比
        double picScale = new BigDecimal(picWidth * 1.0 / picHeight)
                .setScale(2, RoundingMode.HALF_DOWN) //四舍五入，保留两位小数
                .doubleValue(); //转成double类型
        //获取格式
        String picFormat = imageInfo.getFormat();
        //返回封装结果：
        UploadPictureResult uploadPictureResult = new UploadPictureResult();

        uploadPictureResult.setUrl(cosClientConfig.getHost() + "/" + key);
        if (thumbnailCiObject != null)
            uploadPictureResult.setThumbUrl(cosClientConfig.getHost() + "/" + thumbnailCiObject.getKey());
        uploadPictureResult.setName(originalFilename);
        uploadPictureResult.setPicSize(file.length());
        uploadPictureResult.setPicWidth(picWidth);
        uploadPictureResult.setPicHeight(picHeight);
        uploadPictureResult.setPicScale(picScale);
        uploadPictureResult.setPicFormat(picFormat);
        return uploadPictureResult;
    }

    /**
     * 检查图片格式
     *
     * @param inputSource
     */
    protected abstract void checkPictureObject(Object inputSource);

    /**
     * 获取原始文件名
     *
     * @param inputSource
     * @return
     */
    protected abstract String getOriginalFilename(Object inputSource);

    /**
     * 填充本地临时文件
     *
     * @param inputSource
     * @param file
     */
    protected abstract void processFile(Object inputSource, File file) throws IOException;


}
