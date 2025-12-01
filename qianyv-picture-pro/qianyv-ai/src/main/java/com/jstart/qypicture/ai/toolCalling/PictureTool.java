package com.jstart.qypicture.ai.toolCalling;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpException;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import cn.hutool.json.JSONUtil;
import com.jstart.qypicture.ai.entity.QwenImageGenerateRequest;
import com.jstart.qypicture.ai.enums.PictureSizeEnum;
import com.jstart.qypicture.enums.ResultEnum;
import com.jstart.qypicture.exception.BusinessException;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.beans.beancontext.BeanContext;

@Slf4j
@Component
public class PictureTool {

    @Value("${api-key.dashscope}")
    private String dashscopeApiKey;


    @Resource
    private ChatClient chatSummaryClient;


    @Tool(description = "获取图片生成的专业提示词")
    public String getPrompt(ToolContext toolContext) {
        //1. 从上下文中获取conversationId
        Long conversationId = (Long) toolContext.getContext().get("conversationId");
        //2. todo 根据conversationId获取聊天记录等信息（此处省略，假设已经获取到相关信息）

        //3. 调用chatSummaryClient生成专业提示词

        String prompt = null;
        try {
            prompt = chatSummaryClient.prompt()
                    .user("userInput")
                    .call()
                    .content();
        } catch (Exception e) {
            log.error("调用chatSummaryClient生成图片提示词失败：{}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
        log.debug("获取到图片生成提示词：{}", prompt);
        return prompt;
    }

    @Tool(description = "生成图片并获取到图片URL")
    public String generateImage(@ToolParam(description = "图片生成的提示词") String prompt,
                                @ToolParam(description = "图片比例，默认是1:1", required = false) String scale,
                                @ToolParam(description = "反向提示词，无希望出现的内容，例如‘畸形’、‘残缺’", required = false) String negativePrompt
    ) {
        String pictureSize = PictureSizeEnum.getSizeByScale(scale);
        QwenImageGenerateRequest request = new QwenImageGenerateRequest(
                prompt,
                negativePrompt != null ? negativePrompt : "",
                pictureSize,
                false
        );
        String requestBody = JSONUtil.toJsonStr(request);

        String respBody = "";
        try {
             respBody = HttpUtil.createRequest(Method.POST,
                            "https://dashscope.aliyuncs.com/api/v1/services/aigc/multimodal-generation/generation")
                    .body(requestBody)
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + dashscopeApiKey)
                    .timeout(1000 * 60 * 5)
                    .execute()
                    .body();
        } catch (HttpException e) {
            log.error("请求 阿里百炼 图像生成模型失败：{}", e.getMessage(),e);
            return "图像生成失败，告诉用户稍后重试";
        }
        if (!StrUtil.contains(respBody, "image")) {
            log.error("阿里百炼 图像生成模型返回错误：{}", respBody);
            return "图像生成失败，安抚用户";
        }
        //通过表达式获取JSON中嵌套的对象
        String imageUrl = (String)JSONUtil.getByPath(
                JSONUtil.parse(respBody),
                "output.choices[0].message.content[0].image"
        );
        log.info("生成图片成功，图片URL：{}", imageUrl);

        return imageUrl;

    }


}
