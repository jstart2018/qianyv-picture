package com.jstart.qypicture.ai.core;

import com.jstart.qypicture.ai.enums.AiActEnum;
import com.jstart.qypicture.ai.handle.AiActHandler;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.messages.Message;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;

@Component
@Slf4j
public class AiApp {

    @Resource
    private ChatClientFactory chatClientFactory;

    @Resource
    private ChatClient chatRoutingClient;

    @Resource
    private ChatMemoryRepository chatMemoryRepository;

    public Flux<String> doChat(String input,Long conversationId) {
        //1.获取历史对话记录
        List<Message> historyMessage = chatMemoryRepository.findByConversationId(String.valueOf(conversationId));
        //2.拼接用户输入和历史记录作为上下文
        String context = input;
        if (historyMessage != null && !historyMessage.isEmpty()) {
            context = "用户当前输入：" + input + "\n历史对话上下文：" + historyMessage.toString();
        }
        //3.AI路由操作类型，手动序列化
        String routerResult = chatRoutingClient.prompt()
                .user(input)
                .call()
                .content();
        AiActEnum actEnum = AiActEnum.valueOf(routerResult);

        // 4.AI执行操作
        AiActHandler aiActHandler = chatClientFactory.getAiActHandle(actEnum);

        return aiActHandler.act(input,conversationId);
    }



}
