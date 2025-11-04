package com.jstart.qypicture.model.dto;

import com.jstart.qypicture.model.PageRequest;
import lombok.Data;

/**
 * 图片查询请求
 */
@Data
public class PictureDownLoadDTO extends PageRequest {

    /**
     * 图片id
     */
    private Long pictureId;

    /**
     * 空间id，非必须
     */
    private Long spaceId;

}