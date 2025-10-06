package com.jstart.qypicture.template.uploadFileTemplate;


import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IORuntimeException;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import com.jstart.qypicture.enums.ResultEnum;
import com.jstart.qypicture.exception.BusinessException;
import com.jstart.qypicture.utils.ThrowUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
public class UrlPictureUpload extends PictureUploadTemplate {
    /**
     * 校验文件格式
     *
     * @param inputSource
     */
    @Override
    protected void checkPictureObject(Object inputSource) {
        String fileUrl = (String) inputSource;
        ThrowUtils.throwIf(StringUtils.isBlank(fileUrl), ResultEnum.PARAMS_ERROR, "url不能为空");
        //1、校验url格式
        try {
            new URL(fileUrl);
        } catch (MalformedURLException e) {
            throw new BusinessException(ResultEnum.PARAMS_ERROR, "url格式错误");
        }
        //2、校验协议
        ThrowUtils.throwIf(!(fileUrl.startsWith("https://") || fileUrl.startsWith("http://")),
                ResultEnum.PARAMS_ERROR, "仅支持HTTPS和HTTP协议的地址请求");
        //3、对地址发送head请求，获取元信息
        HttpResponse resp = null;
        try {
            resp = HttpUtil.createRequest(Method.HEAD, fileUrl).timeout(5000).execute();
            //4、校验是否请求成功
            if (resp.getStatus() != HttpStatus.HTTP_OK) {
                throw new BusinessException(ResultEnum.OPERATION_ERROR, "请求url错误");
            }
            //5、校验文件格式：
            String contentType = resp.header("Content-Type");
            if (!StringUtils.isBlank(contentType)) {
                final List<String> list = Arrays.asList("image/jpg", "image/png", "image/jpeg", "image/webp", "image/gif");
                ThrowUtils.throwIf(!list.contains(contentType), ResultEnum.OPERATION_ERROR, "文件类型不支持：" + contentType);
            }
            //6、校验文件大小
            String contentLengthStr = resp.header("Content-Length");
            if (!StringUtils.isBlank(contentLengthStr)) {
                try {
                    long contentLength = Long.parseLong(contentLengthStr);
                    final long MAX_PICTURE_SIZE = 1024 * 1024 * 5;
                    ThrowUtils.throwIf(contentLength > MAX_PICTURE_SIZE,
                            ResultEnum.OPERATION_ERROR, "图片大小不可超过5MB");
                } catch (NumberFormatException e) {
                    throw new BusinessException(ResultEnum.PARAMS_ERROR, "文件大小格式错误");
                }
            }
        } catch (IORuntimeException e) {
            log.error(e.getMessage(), e);
            throw new BusinessException(ResultEnum.OPERATION_ERROR, "文件请求超时，可能不允许访问");
        } finally {
            //释放资源respond
            if (resp != null) {
                resp.close();
            }
        }
    }

    /**
     * 获取原始文件名
     *
     * @param inputSource
     * @return
     */
    @Override
    protected String getOriginalFilename(Object inputSource) {
        String fileUrl = (String) inputSource;
        return FileUtil.getName(fileUrl);
    }

    /**
     * 将临时文件file填充
     *
     * @param inputSource 输入源
     * @param file        临时文件
     * @throws IOException
     */
    @Override
    protected void processFile(Object inputSource, File file) throws IOException {
        String fileUrl = (String) inputSource;
        HttpUtil.downloadFile(fileUrl, file);
    }


}
