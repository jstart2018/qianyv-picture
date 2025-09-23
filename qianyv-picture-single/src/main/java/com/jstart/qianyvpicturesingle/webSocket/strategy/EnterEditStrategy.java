package com.jstart.qianyvpicturesingle.webSocket.strategy;


import com.jstart.qianyvpicturesingle.model.entity.Picture;
import com.jstart.qianyvpicturesingle.webSocket.handle.PictureEditHandle;
import com.jstart.qianyvpicturesingle.webSocket.model.PictureEditMessageTypeEnum;
import com.jstart.qianyvpicturesingle.webSocket.model.PictureEditResponseMessage;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import javax.annotation.Resource;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class EnterEditStrategy implements PictureEditActionStrategy {

    @Resource
    private PictureEditHandle pictureEditHandle;


    @Override
    public PictureEditMessageTypeEnum getPictureEditActionEnum() {
        return PictureEditMessageTypeEnum.ENTER_EDIT;
    }

    @Override
    public void executeStrategy(ConcurrentHashMap<Long, Set<WebSocketSession>> pictureEditRome, WebSocketSession excludeSession, PictureEditResponseMessage responseMessage, Picture picture) {
    }


}
