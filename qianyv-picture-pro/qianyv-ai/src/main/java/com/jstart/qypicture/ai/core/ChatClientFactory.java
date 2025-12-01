package com.jstart.qypicture.ai.core;

import com.jstart.qypicture.ai.enums.AiActEnum;
import com.jstart.qypicture.ai.handle.AiActHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Slf4j
public class ChatClientFactory implements InitializingBean {

    @Autowired
    private List<AiActHandler> aiActHandlerList;

    private static Map<String,AiActHandler> aiActHandlerMap;

    protected static AiActHandler getAiActHandle(AiActEnum aiActEnum) {
        // 根据AiActEnum的key获取对应的ChatClient
        AiActHandler aiActHandler = aiActHandlerMap.get(aiActEnum.getName());
        if (aiActHandler == null) {
            log.error("未找到AI行为枚举对应的执行实例，AiActEnum：{}", aiActEnum);
            throw new RuntimeException("抱歉，该功能暂未支持。");
        }
        return aiActHandler;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (aiActHandlerList == null || aiActHandlerList.isEmpty()) {
            log.error("未找到任何AI行为处理器实例，请检查配置");
            throw new RuntimeException("系统错误，未找到任何AI行为处理器实例");
        }
        aiActHandlerMap = aiActHandlerList.stream()
                .collect(Collectors.toMap(AiActHandler::getActName, handler -> handler));
    }
}
