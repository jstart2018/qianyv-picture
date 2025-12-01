package com.jstart.qypicture.service.impl;

import com.jstart.qypicture.ai.core.AiApp;
import com.jstart.qypicture.service.AiService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class AiServiceImpl implements AiService {

    @Resource
    private AiApp aiApp;

    /**
     * 多功能AI聊天
     * @param input
     * @return
     */
    @Override
    public Flux<String> doChat(String input,Long conversationId) {
        //todo 会话id待校验

        return aiApp.doChat(input,conversationId);
    }
}
