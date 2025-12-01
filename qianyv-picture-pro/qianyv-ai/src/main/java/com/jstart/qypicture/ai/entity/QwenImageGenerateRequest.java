package com.jstart.qypicture.ai.entity;

import java.util.ArrayList;
import java.util.List;

/**
 * 通义千问图像生成请求体（简化外部构建）
 */
public class QwenImageGenerateRequest {

    private final String model = "qwen-image-plus";

    private final Input input;

    private final Parameters parameters;

    public QwenImageGenerateRequest(String text, String negative_prompt, String size, boolean prompt_extend) {
        this.input = new Input(text);
        this.parameters = new Parameters(negative_prompt, prompt_extend, size);
    }

    private class Input {
        private final List<Message> messages= new ArrayList<>();

        public Input(String text) {
            Content content = new Content(text);
            List<Content> contentList = new ArrayList<>();
            contentList.add(content);

            // 构建 message（固定 role 为 user）
            Message message = new Message(contentList);
            this.messages.add(message);
        }
        // getter（序列化需要，fastjson 会自动获取字段值）
        public List<Message> getMessages() {
            return messages;
        }
    }

    /**
     * 消息内部类
     */
    private static class Message {
        private final String role = "user";

        private final List<Content> content;

        public Message(List<Content> content) {
            this.content = content;
        }

        // getter（序列化需要）
        public String getRole() {
            return role;
        }
        public List<Content> getContent() {
            return content;
        }
    }

    /**
     * 内容内部类（仅包含 text 字段）
     */
    private static class Content {
        private final String text;

        public Content(String text) {
            this.text = text;
        }

        // getter（序列化需要）
        public String getText() {
            return text;
        }
    }

    /**
     * 配置参数内部类
     */
    private static class Parameters {

        // 负面提示词，默认空字符串
        private String negativePrompt = "低分辨率、错误、最差质量、低质量、残缺、畸形、多余的手指、比例不良";
        //提示词智能改写
        private boolean promptExtend = false;
        // qwen的水印，默认 false
        private boolean watermark = false;
        // 图片尺寸
        private final String size;

        public Parameters(String negativePrompt, boolean promptExtend, String size) {
            // 负面提示词默认空字符串（避免 null）
            this.negativePrompt = this.negativePrompt + negativePrompt ;
            this.promptExtend = promptExtend;
            this.size = size;
        }

        // getter（序列化需要）
        public String getNegativePrompt() {
            return negativePrompt;
        }
        public boolean isPromptExtend() {
            return promptExtend;
        }
        public boolean isWatermark() {
            return watermark;
        }
        public String getSize() {
            return size;
        }
    }

    // ==================== 顶级字段 getter（序列化需要）====================
    public String getModel() {
        return model;
    }
    public Input getInput() {
        return input;
    }
    public Parameters getParameters() {
        return parameters;
    }
}