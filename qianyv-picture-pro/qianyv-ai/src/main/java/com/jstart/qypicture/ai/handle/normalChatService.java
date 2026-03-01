package com.jstart.qypicture.ai.handle;

import com.jstart.qypicture.ai.enums.AiActEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

/**
 * 普通聊天服务实现
 */
@Service
@Slf4j
public class normalChatService implements AiActHandler {

    @Resource
    private ChatClient normalChatClient;

    @Override
    public String getActName() {
        return AiActEnum.NORMAL_CHAT.getName();
    }

    @Override
    public Flux<String> act(String input, Long conversationId) {
        //调用AI获取结果
        return normalChatClient.prompt()
                .user(input)
                .advisors(memory -> memory.param(ChatMemory.CONVERSATION_ID, conversationId))
                .stream()
                .content();

    }
}
