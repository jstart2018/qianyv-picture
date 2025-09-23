package com.jstart.qianyvpicturesingle.webSocket.strategy;


import com.jstart.qianyvpicturesingle.model.entity.Picture;
import com.jstart.qianyvpicturesingle.webSocket.model.PictureEditMessageTypeEnum;
import com.jstart.qianyvpicturesingle.webSocket.model.PictureEditResponseMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public interface PictureEditActionStrategy {

    PictureEditMessageTypeEnum getPictureEditActionEnum();

    void executeStrategy(ConcurrentHashMap<Long, Set<WebSocketSession>> pictureEditRome, WebSocketSession excludeSession, PictureEditResponseMessage responseMessage, Picture picture);

}
