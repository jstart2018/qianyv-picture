package com.jstart.qypicture.ai.config;


import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * 多个兼容openAI的API的大模型配置
 */
@Configuration
@Slf4j
public class ChatModelConfig {

    @Value("${ai.dashscope.base-url}")
    private String dashScopeBaseUrl;

    @Value("${ai.dashscope.api-key}")
    private String dashScopeApiKey;

    @Value("${ai.deepseek.base-url}")
    private String deepSeekBaseUrl;

    @Value("${ai.deepseek.api-key}")
    private String deepSeekApiKey;




    @Resource
    private OpenAiChatModel baseChatModel;

    @Resource
    private OpenAiApi baseOpenAiApi;

    @Bean
    public ChatModel qwenFlashChatModel() {
        log.info("初始化qwenFlashChatModel...");
        try {
            return baseChatModel.mutate()
                    .openAiApi(baseOpenAiApi.mutate()
                            .baseUrl(dashScopeBaseUrl)
                            .apiKey(dashScopeApiKey)
                            .build())
                    .defaultOptions(OpenAiChatOptions.builder()
                            .model("qwen-flash")
                            .build())
                    .build();
        } catch (Exception e) {
            log.error("初始化qwenFlashChatModel失败");
            throw new RuntimeException(e);
        }
    }

    @Bean
    public ChatModel qwen3MaxChatModel() {
        log.info("初始化qwen3MaxChatModel...");
        try {
            return baseChatModel.mutate()
                    .openAiApi(baseOpenAiApi.mutate()
                            .baseUrl(dashScopeBaseUrl)
                            .apiKey(dashScopeApiKey)
                            .build())
                    .defaultOptions(OpenAiChatOptions.builder()
                            .model("qwen3-max")
                            .build())
                    .build();
        } catch (Exception e) {
            log.error("初始化qwen3MaxChatModel失败");
            throw new RuntimeException(e);
        }
    }

    @Bean
    public ChatModel qwen3VlPlusChatModel() {
        log.info("初始化qwen3VlPlusChatModel...");
        try {
            return baseChatModel.mutate()
                    .openAiApi(baseOpenAiApi.mutate()
                            .baseUrl(dashScopeBaseUrl)
                            .apiKey(dashScopeApiKey)
                            .build())
                    .defaultOptions(OpenAiChatOptions.builder()
                            .model("qwen3-vl-plus")
                            .build())
                    .build();
        } catch (Exception e) {
            log.error("初始化qwen3VlPlusChatModel失败");
            throw new RuntimeException(e);
        }
    }

    @Bean
    public ChatModel deepSeekV3ChatModel() {
        log.info("初始化deepSeekV3ChatModel...");
        try {
            return baseChatModel.mutate()
                    .openAiApi(baseOpenAiApi.mutate()
                            .baseUrl(deepSeekBaseUrl)
                            .apiKey(deepSeekApiKey)
                            .build())
                    .defaultOptions(OpenAiChatOptions.builder()
                            .model("deepseek-chat")
                            .temperature(0.5)
                            .build())
                    .build();
        } catch (Exception e) {
            log.error("初始化deepSeekV3ChatModel失败");
            throw new RuntimeException(e);
        }
    }
}
