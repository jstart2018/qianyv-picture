package com.jstart.qypicture.controller;


import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.json.JSONUtil;
import com.jstart.qypicture.result.Result;
import com.jstart.qypicture.service.AiService;
import jakarta.annotation.Resource;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/app")
public class AppController {

    @Resource
    private AiService aiService;

    /**
     * 创建会话
     *
     * @return
     */
    @GetMapping("/create/session")
    public Result<Long> createSession() {

        return Result.success(StpUtil.getLoginIdAsLong());
    }


    /**
     * 多功能AI聊天
     *
     * @param input
     * @return
     */
    @PostMapping("/chat/{conversationId}")
    public Flux<ServerSentEvent<String>> doChat(String input,Long conversationId) {
        return aiService.doChat(input,conversationId)
                .map(data -> {
                    //数据封装为JSON格式，避免特殊字符导致事件解析错误
                    Map<String, String> dataMap = Map.of("d", data);
                    String dataJson = JSONUtil.toJsonStr(dataMap);
                    return ServerSentEvent.builder(dataJson).build();
                })
                //添加结束标记事件
                .concatWith(Mono
                        .just(ServerSentEvent.<String>builder()
                                .event("done")
                                .data("")
                                .build()));

    }

}
