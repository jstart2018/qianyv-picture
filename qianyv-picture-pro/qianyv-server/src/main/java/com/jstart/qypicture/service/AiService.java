package com.jstart.qypicture.service;

import reactor.core.publisher.Flux;

public interface AiService {


    Flux<String> doChat(String input,Long conversationId);
}
