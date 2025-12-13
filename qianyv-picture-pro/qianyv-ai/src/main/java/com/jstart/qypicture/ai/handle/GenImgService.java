package com.jstart.qypicture.ai.handle;

import cn.dev33.satoken.stp.StpUtil;
import com.jstart.qypicture.ai.enums.AiActEnum;
import com.jstart.qypicture.ai.toolCalling.PictureTool;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.Map;

@Service
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
        return generatePictureClient.prompt()
                .user(input)
                .advisors(advisor -> advisor.param("conversationId", conversationId))
                .tools(pictureTool)//工具调用生成图片

                .toolContext(Map.of("conversationId", conversationId,//工具上下文，传入会话id给工具使用
                                    "tokenValue", StpUtil.getTokenValue()))
                .stream()
                .content();
    }
}
