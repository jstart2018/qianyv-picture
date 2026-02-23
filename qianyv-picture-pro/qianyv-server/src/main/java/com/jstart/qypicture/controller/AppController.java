package com.jstart.qypicture.controller;


import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.jstart.qypicture.enums.ResultEnum;
import com.jstart.qypicture.model.dto.ChatMemoryQueryDTO;
import com.jstart.qypicture.model.vo.ChatMemoryVO;
import com.jstart.qypicture.result.Result;
import com.jstart.qypicture.service.AiService;
import com.jstart.qypicture.service.SpringAiChatMemoryService;
import com.jstart.qypicture.utils.ThrowUtils;
import jakarta.annotation.Resource;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/app")
public class AppController {

    @Resource
    private AiService aiService;
    
    @Resource
    private SpringAiChatMemoryService springAiChatMemoryService;

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

    /**
     * 获取AI聊天记录（游标分页）
     *
     * @param queryDTO 查询参数（conversationId必传，cursor首次不传，pageSize默认20）
     * @return 聊天记录列表
     */
    @PostMapping("/chat/history")
    public Result<List<ChatMemoryVO>> getChatHistory(@RequestBody ChatMemoryQueryDTO queryDTO) {
        ThrowUtils.throwIf(queryDTO == null || StrUtil.isBlank(queryDTO.getConversationId()),
                ResultEnum.PARAMS_ERROR, "会话ID不能为空");
        
        // 限制pageSize最大值
        if (queryDTO.getPageSize() == null || queryDTO.getPageSize() <= 0) {
            queryDTO.setPageSize(10);
        } else if (queryDTO.getPageSize() > 100) {
            queryDTO.setPageSize(100);
        }
        
        List<ChatMemoryVO> historyList = springAiChatMemoryService.listByCursor(queryDTO);
        
        return Result.success(historyList);
    }

}
