package com.jstart.qypicture.ai.handle;

import cn.dev33.satoken.stp.StpUtil;
import com.jstart.qypicture.ai.enums.AiActEnum;
import com.jstart.qypicture.ai.toolCalling.PictureTool;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.Map;

@Service
@Slf4j
public class GenImgService implements AiActHandler {

    @Resource
    private ChatClient generatePictureClient;

    @Resource
    private PictureTool pictureTool;

    @Override
    public String getActName() {
        return AiActEnum.GENERATE_IMAGE.getName();
    }

    @Override
    public Flux<String> act(String input, Long conversationId) {
        // 使用同步调用避免Spring AI流式模式下Tool Calling的bug
        // 然后包装成Flux返回，在单独线程执行避免阻塞主线程
        String tokenValue = StpUtil.getTokenValue();
        return Flux.defer(() -> {
            try {
                String content = generatePictureClient.prompt()
                        .user(input)
                        .advisors(memory -> memory.param(ChatMemory.CONVERSATION_ID, conversationId))
                        .advisors(advisor -> advisor.param("conversationId", conversationId))
                        .tools(pictureTool)
                        .toolContext(Map.of("conversationId", conversationId,
                                "tokenValue", tokenValue))
                        .call()  // 使用同步调用而不是 stream()
                        .content();
                log.info("图片生成完成，AI响应内容长度：{}", content != null ? content.length() : 0);
                return Flux.just(content != null ? content : "");
            } catch (Exception e) {
                log.error("图片生成失败：{}", e.getMessage(), e);
                return Flux.just("抱歉，图片生成失败，请稍后重试。");
            }
        }).subscribeOn(Schedulers.boundedElastic()); // 在弹性线程池执行，避免阻塞
    }
}
