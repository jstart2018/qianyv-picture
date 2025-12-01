package com.jstart.qypicture.ai.config;


import com.jstart.qypicture.utils.ResourceTxtReaderUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class ChatClientConfig {

    /**
     * 路由用户意图AI（qwen-flash）
     */
    @Bean
    public ChatClient chatRoutingClient(ChatModel qwenFlashChatModel) {
        log.info("初始化 chatRoutingChatClient(路由用户意图AI)...");
        return ChatClient.builder(qwenFlashChatModel)
                .defaultSystem(ResourceTxtReaderUtils
                        .loadPromptFromResource("prompt/chat-routing-system-prompt.txt"))
                .build();
    }


    /**
     * 聊天总结图像意图AI（qwen3-max）
     */
    @Bean
    public ChatClient chatSummaryClient(ChatModel qwen3MaxChatModel) {
        log.info("初始化 chatSummaryChatClient(聊天总结图像意图AI)...");
        String sysPrompt = ResourceTxtReaderUtils
                .loadPromptFromResource("prompt/chat-summary-system-prompt.txt");
        return ChatClient.builder(qwen3MaxChatModel)
                .defaultSystem(sysPrompt)
                .build();
    }

    /**
     * 生成图片模型（qwen-flash）
     */
    @Bean
    public ChatClient generatePictureClient(ChatModel qwenFlashChatModel, JdbcChatMemoryRepository chatMemoryRepository) {
        log.info("初始化 generatePictureClient(图片生成AI)...");
        String sysPrompt = ResourceTxtReaderUtils
                .loadPromptFromResource("prompt/create-picture-system-prompt.txt");
        return ChatClient.builder(qwenFlashChatModel)
                .defaultSystem(sysPrompt)
                .defaultAdvisors(MessageChatMemoryAdvisor
                        .builder(MessageWindowChatMemory
                                .builder()
                                .chatMemoryRepository(chatMemoryRepository)
                                .maxMessages(20)
                                .build())
                        .build())
                .build();
    }

    /**
     * 视觉模型，解析图片（qwen3-vl-plus）
     */
    @Bean
    public ChatClient parsePictureClient(ChatModel qwen3VlPlusChatModel) {
        log.info("初始化 parsePictureChatClient(图片解析AI)...");
        String sysPrompt = ResourceTxtReaderUtils
                .loadPromptFromResource("prompt/parse-picture-system-prompt.txt");
        return ChatClient.builder(qwen3VlPlusChatModel)
                .defaultSystem(sysPrompt)
                .build();
    }

    /**
     * 普通聊天AI（deepseek-v3）
     */
    @Bean
    public ChatClient normalChatClient(ChatModel deepSeekV3ChatModel, JdbcChatMemoryRepository chatMemoryRepository) {
        log.info("初始化 normalChatClient(普通聊天AI)...");
        String sysPrompt = ResourceTxtReaderUtils
                .loadPromptFromResource("prompt/normal-chat.txt");
        return ChatClient.builder(deepSeekV3ChatModel)
                .defaultSystem(sysPrompt)
                .defaultAdvisors(MessageChatMemoryAdvisor
                        .builder(MessageWindowChatMemory
                                .builder()
                                .chatMemoryRepository(chatMemoryRepository)
                                .maxMessages(20)
                                .build())
                        .build())
                .build();
    }

}
