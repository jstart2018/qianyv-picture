package com.jstart.qypicture.handler.picture;


import com.jstart.qypicture.enums.PicturePlaceEnum;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Component
public class PictureHandlerFactory implements InitializingBean {

    @Resource
    private List<PictureHandler<?>> pictureHandlers = new LinkedList<>();

    Map<PicturePlaceEnum, PictureHandler<?>> pictureHandlerMap = new HashMap<>();


    public PictureHandler<?> getPictureSpaceHandler(PicturePlaceEnum picturePlaceEnum) {
        return pictureHandlerMap.get(picturePlaceEnum);
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        for (PictureHandler<?> pictureHandler : pictureHandlers) {
            pictureHandlerMap.put(pictureHandler.getPicturePlaceEnum(), pictureHandler);
        }


    }
}
