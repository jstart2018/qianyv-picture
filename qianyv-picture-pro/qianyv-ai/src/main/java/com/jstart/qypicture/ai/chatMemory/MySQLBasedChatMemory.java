package com.jstart.qypicture.ai.chatMemory;

import cn.hutool.core.collection.CollectionUtil;
import com.google.gson.Gson;
import com.jstart.qypicture.model.entity.SpringAiChatMemory;
import com.jstart.qypicture.service.SpringAiChatMemoryService;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Component;
import org.springframework.ai.chat.messages.Message;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class MySQLBasedChatMemory implements ChatMemory {

    private final SpringAiChatMemoryService springAiChatMemoryService;

    public MySQLBasedChatMemory(SpringAiChatMemoryService springAiChatMemoryService) {
        this.springAiChatMemoryService = springAiChatMemoryService;
    }


    @Override
    public void add(String conversationId, List<Message> messages) {
        Gson gson = new Gson();
        List<SpringAiChatMemory> conversationMemories = messages.stream().map(message -> {
            String messageType = message.getMessageType().getValue();
            String mes = gson.toJson(message);
            return SpringAiChatMemory.builder().conversationId(conversationId)
                    .type(messageType).content(mes).timestamp(new Date()).build();
        }).toList();
        springAiChatMemoryService.saveBatch(conversationMemories);
    }


    @Override
    public List<Message> get(String conversationId) {
        List<SpringAiChatMemory> messages = springAiChatMemoryService.listByConversationId(conversationId);
        if (CollectionUtil.isEmpty(messages)) {
            return List.of();
        }
        return messages.stream()
                .skip(Math.max(0, messages.size() - AiMemoryWindows.AI_MEMORY_SIZE))
                .map(this::getMessage)
                .collect(Collectors.toList());
    }

    @Override
    public void clear(String conversationId) {
        springAiChatMemoryService.deleteByConversationId(conversationId);
    }

    private Message getMessage(SpringAiChatMemory springAiChatMemory) {
        String memory = springAiChatMemory.getContent();
        Gson gson = new Gson();
        return (Message) gson.fromJson(memory, MessageTypeEnum.fromValue(springAiChatMemory.getType()).getClazz());
    }
}
