package com.jstart.qypicture.ai.handle;

import com.jstart.qypicture.ai.enums.AiActEnum;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.ChatClient;
import reactor.core.publisher.Flux;

public class ParseImgService implements AiActHandler {

    @Resource
    private ChatClient parsePictureClient;

    @Override
    public String getActName() {
        return null;
    }

    /**
     * 图片解析
     * @param input 图片内容
     * @param conversationId 冗余会话id
     * @return 解析结果
     */
    @Override
    public Flux<String> act(String input,Long conversationId) {
        return Flux.empty();
    }
}
