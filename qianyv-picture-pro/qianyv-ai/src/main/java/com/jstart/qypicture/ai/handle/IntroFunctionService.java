package com.jstart.qypicture.ai.handle;

import com.jstart.qypicture.ai.enums.AiActEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

/**
 * 普通聊天服务实现
 */
@Service
@Slf4j
public class IntroFunctionService implements AiActHandler {

    @Resource
    private ChatClient introFunctionClient;
    @Resource
    private Advisor introRetrievalAugmentation;

    @Override
    public String getActName() {
        return AiActEnum.INTRO_FUNCTION.getName();
    }

    @Override
    public Flux<String> act(String input, Long conversationId) {
        //调用AI获取结果
        return introFunctionClient.prompt()
                .user(input)
                .advisors(memory -> memory.param(ChatMemory.CONVERSATION_ID, conversationId))
                .advisors(introRetrievalAugmentation) //检索增强
                .stream()
                .content();

    }
}
