package com.jstart.qypicture.ai.handle;

import reactor.core.publisher.Flux;

public interface AiActHandler {

    /**
     * 获取AI行为名称
     * @return AI行为名称
     */
    String getActName();

    /**
     * 执行AI行为
     *
     * @param input 输入内容
     * @return 输出内容
     */
    Flux<String> act(String input,Long conversationId);

}
