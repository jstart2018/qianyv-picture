package com.jstart.qypicture.service;

import reactor.core.publisher.Flux;

import java.nio.channels.FileChannel;

public interface AiService {


    Flux<String> doChat(String input,Long conversationId);
}
