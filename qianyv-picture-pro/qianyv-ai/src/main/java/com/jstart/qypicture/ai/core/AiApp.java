package com.jstart.qypicture.ai.core;

import com.jstart.qypicture.ai.enums.AiActEnum;
import com.jstart.qypicture.ai.handle.AiActHandler;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Component
@Slf4j
public class AiApp {

    @Resource
    private ChatClientFactory chatClientFactory;

    @Resource
    private ChatClient chatRoutingClient;

    public Flux<String> doChat(String input,Long conversationId) {
        //AI路由操作类型，手动序列化
        String routerResult = chatRoutingClient.prompt()
                .user(input)
                .call()
                .content();
        AiActEnum actEnum = AiActEnum.valueOf(routerResult);

        // AI执行操作
        AiActHandler aiActHandler = chatClientFactory.getAiActHandle(actEnum);
        //todo 会话id待补充
        return aiActHandler.act(input,1L);
    }



}
