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
public class SendImgService implements AiActHandler {

    @Resource
    private ChatClient sendImgClient;
    @Resource
    private Advisor sendImgRetrievalAugmentation;

    @Override
    public String getActName() {
        return AiActEnum.SEND_IMG.getName();
    }

    @Override
    public Flux<String> act(String input, Long conversationId) {
        //调用AI获取结果
        return sendImgClient.prompt()
                .user(input)
                .advisors(memory -> memory.param(ChatMemory.CONVERSATION_ID, conversationId))
                .advisors(sendImgRetrievalAugmentation) //检索增强
                .stream()
                .content();

    }
}
